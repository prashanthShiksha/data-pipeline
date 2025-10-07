import os
import json
import psycopg2
import logging
from kafka import KafkaProducer
from datetime import datetime, timedelta
from collections import defaultdict
import json as pyjson
from datetime import  timezone
import configparser


# Load config.ini
config = configparser.ConfigParser()
config.read("config.ini")

# --- DB Configs ---
MENTORING = dict(config["SOURCE_DB1"])
USERS = dict(config["SOURCE_DB2"])

# --- Kafka Config ---
KAFKA_BROKER = config["KAFKA"]["broker"]
TOPIC = config["KAFKA"]["topic"]

producer = KafkaProducer(
    bootstrap_servers=[KAFKA_BROKER],
    value_serializer=lambda v: pyjson.dumps(v, default=str).encode("utf-8"),
    acks=config["KAFKA"].get("acks", "all"),
    linger_ms=int(config["KAFKA"].get("linger_ms", 50)),
    batch_size=int(config["KAFKA"].get("batch_size", 64000)),
    retries=int(config["KAFKA"].get("retries", 5)),
)

def push_event(event: dict):
    """Push JSON event to Kafka"""
    try:
        future = producer.send(TOPIC, event)
        future.get(timeout=10)  # wait for Kafka ack
        logging.info(f"Event delivered: {event.get('entity', 'unknown')} - {event.get('eventType', 'unknown')}")
    except Exception as e:
        logging.error(f"Failed to deliver event to Kafka: {e}")
        raise

# ---------------- Logging Setup ----------------
LOG_DIR = config["LOGGING"].get("log_dir", "logs")  # fallback to "logs" if missing
RETENTION_DAYS = int(config["LOGGING"].get("retention_days", 7))

os.makedirs(LOG_DIR, exist_ok=True)

log_file = os.path.join(LOG_DIR, f"batch_run_{datetime.now(timezone.utc).strftime('%Y%m%d_%H%M%S')}.log")

logging.basicConfig(
    filename=log_file,
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)

# Cleanup logs older than retention_days
for f in os.listdir(LOG_DIR):
    fpath = os.path.join(LOG_DIR, f)
    if os.path.isfile(fpath):
        mtime = datetime.fromtimestamp(os.path.getmtime(fpath), timezone.utc)
        if mtime < datetime.now(timezone.utc) - timedelta(days=RETENTION_DAYS):
            os.remove(fpath)

# ---------------- State Management ----------------
STATE_FILE = "last_run.json"

def get_last_run(entity: str):
    """Get last run timestamp for an entity. Return None if fresh run."""
    if not os.path.exists(STATE_FILE):
        logging.info(f"No state file found. Fresh run for {entity}")
        return None
    with open(STATE_FILE, "r") as f:
        state = json.load(f)
    return state.get(entity)

def update_last_run(entity: str, ts: datetime):
    """Update last run timestamp for entity"""
    state = {}
    if os.path.exists(STATE_FILE):
        with open(STATE_FILE, "r") as f:
            state = json.load(f)
    state[entity] = ts.isoformat()
    with open(STATE_FILE, "w") as f:
        json.dump(state, f, indent=2)

# ---------------- Sessions ----------------
def process_sessions():
    logging.info("Processing sessions...")
    conn = psycopg2.connect(**MENTORING)
    cur = conn.cursor()

    last_run = get_last_run("sessions")

    if last_run is None:
        cur.execute("""
            SELECT id, mentor_id, title, description, tenant_code, type, status,
                   mentor_organization_id, meeting_info, started_at, completed_at,
                   created_at, updated_at, deleted_at, recommended_for,
                   categories, medium, created_by, updated_by
            FROM sessions
        """)
    else:
        cur.execute("""
            SELECT id, mentor_id, title, description, tenant_code, type, status,
                   mentor_organization_id, meeting_info, started_at, completed_at,
                   created_at, updated_at, deleted_at, recommended_for,
                   categories, medium, created_by, updated_by
            FROM sessions
            WHERE created_at > %s OR updated_at > %s OR (deleted_at IS NOT NULL AND deleted_at > %s)
        """, (last_run, last_run, last_run))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} sessions")

    for row in rows:
        (
            session_id, mentor_id, title, description, tenant_code, type, status,
            mentor_organization_id, meeting_info, started_at, completed_at,
            created_at, updated_at, deleted_at, recommended_for,
            categories, medium, created_by, updated_by
        ) = row

        platform = None
        if meeting_info:
            try:
                if isinstance(meeting_info, dict):
                    platform = meeting_info.get("platform")
                else:
                    meeting_data = pyjson.loads(meeting_info)
                    platform = meeting_data.get("platform")
            except Exception as e:
                logging.error(f"Error parsing meeting_info for session {session_id}: {e}")

        org_id, org_name, org_code = None, None, None
        if mentor_organization_id:
            try:
                cur_org = conn.cursor()
                cur_org.execute(
                    """SELECT organization_id, name, organization_code
                       FROM organization_extension
                       WHERE organization_id = %s""",
                    (mentor_organization_id,)
                )
                org_row = cur_org.fetchone()
                if org_row:
                    org_id, org_name, org_code = org_row
                cur_org.close()
            except Exception as e:
                logging.error(f"Error fetching org {mentor_organization_id}: {e}")

        event = {
            "eventType": "create",
            "entity": "session",
            "session_id": session_id,
            "mentor_id": mentor_id,
            "name": title,
            "description": description,
            "tenant_code": tenant_code,
            "type": type,
            "status": status,
            "org_id": org_id,
            "org_code": org_code,
            "org_name": org_name,
            "platform": platform,
            "started_at": started_at,
            "completed_at": completed_at,
            "created_at": created_at,
            "updated_at": updated_at,
            "deleted_at": deleted_at,
            "recommended_for": recommended_for,
            "categories": categories,
            "medium": medium,
            "created_by": created_by,
            "updated_by": updated_by,
            "deleted": deleted_at is not None
        }
        push_event(event)
        producer.flush()

    update_last_run("sessions", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Attendance ----------------
def process_attendance():
    logging.info("Processing attendance...")
    conn = psycopg2.connect(**MENTORING)
    cur = conn.cursor()
    last_run = get_last_run("attendance")

    if last_run is None:
        cur.execute("""
            SELECT id, tenant_code, session_id, mentee_id, joined_at, left_at,
                   is_feedback_skipped, type, created_at, updated_at, deleted_at
            FROM session_attendees
        """)
    else:
        cur.execute("""
            SELECT id, tenant_code, session_id, mentee_id, joined_at, left_at,
                   is_feedback_skipped, type, created_at, updated_at, deleted_at
            FROM session_attendees
            WHERE created_at > %s OR updated_at > %s OR (deleted_at IS NOT NULL AND deleted_at > %s)
        """, (last_run, last_run, last_run))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} attendance records")

    for row in rows:
        (att_id, tenant_code, session_id, mentee_id, joined_at, left_at,
         is_feedback_skipped, type, created_at, updated_at, deleted_at) = row

        event = {
            "eventType": "create",
            "entity": "attendance",
            "att_id": att_id,
            "tenant_code": tenant_code,
            "session_id": session_id,
            "mentee_id": mentee_id,
            "joined_at": joined_at,
            "left_at": left_at,
            "is_feedback_skipped": is_feedback_skipped,
            "type": type,
            "created_at": created_at,
            "updated_at": updated_at,
            "deleted_at": deleted_at,
            "deleted": deleted_at is not None
        }
        push_event(event)
        producer.flush()

    update_last_run("attendance", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- From Connections Table for Status as ACCEPTED ----------------
def process_connections():
    logging.info("Processing connections...")
    conn = psycopg2.connect(**MENTORING)
    cur = conn.cursor()
    last_run = get_last_run("connections")

    if last_run is None:
        cur.execute("""
            SELECT c.id, c.tenant_code, c.user_id, c.friend_id, c.status,
                   c.created_by, c.updated_by, c.created_at, c.updated_at,
                   ue.organization_id, c.deleted_at
            FROM connections c
            LEFT JOIN user_extensions ue ON c.user_id = ue.user_id
        """)
    else:
        cur.execute("""
            SELECT c.id, c.tenant_code, c.user_id, c.friend_id, c.status,
                   c.created_by, c.updated_by, c.created_at, c.updated_at,
                   ue.organization_id, c.deleted_at
            FROM connections c
            LEFT JOIN user_extensions ue ON c.user_id = ue.user_id
            WHERE c.created_at > %s OR c.updated_at > %s OR (c.deleted_at IS NOT NULL AND c.deleted_at > %s)
        """, (last_run, last_run, last_run))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} connections")

    for row in rows:
        (conn_id, tenant_code, user_id, friend_id, status,
         created_by, updated_by, created_at, updated_at, org_id, deleted_at) = row

        event = {
            "eventType": "create",
            "entity": "connections",
            "conn_id": conn_id,
            "tenant_code": tenant_code,
            "user_id": user_id,
            "friend_id": friend_id,
            "status": status,
            "org_id": org_id,
            "created_by": created_by,
            "updated_by": updated_by,
            "created_at": created_at,
            "updated_at": updated_at,
            "deleted_at": deleted_at,
            "deleted": deleted_at is not None
        }
        push_event(event)
        producer.flush()

    update_last_run("connections", datetime.now(timezone.utc))

    cur.close()
    conn.close()

# ---------------- From Connection_Requests Table for Status as REQUESTED and REJECTED ----------------
def process_connection_requests():
        logging.info("Processing connection_requests...")
        conn = psycopg2.connect(**MENTORING)
        cur = conn.cursor()
        last_run = get_last_run("connection_requests")

        if last_run is None:
            cur.execute("""
                SELECT cr.id, cr.tenant_code, cr.user_id, cr.friend_id, cr.status,
                       cr.created_by, cr.updated_by, cr.created_at, cr.updated_at,
                       ue.organization_id, cr.deleted_at
                FROM connection_requests cr
                LEFT JOIN user_extensions ue ON cr.user_id = ue.user_id
            """)
        else:
            cur.execute("""
                SELECT cr.id, cr.tenant_code, cr.user_id, cr.friend_id, cr.status,
                       cr.created_by, cr.updated_by, cr.created_at, cr.updated_at,
                       ue.organization_id, cr.deleted_at
                FROM connection_requests cr
                LEFT JOIN user_extensions ue ON cr.user_id = ue.user_id
                WHERE cr.created_at > %s OR cr.updated_at > %s OR (cr.deleted_at IS NOT NULL AND cr.deleted_at > %s)
            """, (last_run, last_run, last_run))

        rows = cur.fetchall()
        logging.info(f"Fetched {len(rows)} connection_requests")

        for row in rows:
            (conn_id, tenant_code, user_id, friend_id, status,
             created_by, updated_by, created_at, updated_at, org_id, deleted_at) = row

            event = {
                "eventType": "create",
                "entity": "connections",
                "conn_id": conn_id,
                "tenant_code": tenant_code,
                "user_id": user_id,
                "friend_id": friend_id,
                "status": status,
                "org_id": org_id,
                "created_by": created_by,
                "updated_by": updated_by,
                "created_at": created_at,
                "updated_at": updated_at,
                "deleted_at": deleted_at,
                "deleted": deleted_at is not None
            }
            push_event(event)
            producer.flush()

        update_last_run("connection_requests", datetime.now(timezone.utc))

        cur.close()
        conn.close()

# ---------------- Org Mentor Ratings ----------------
def process_orgMentorRatings():
    logging.info("Processing org mentor ratings...")
    conn = psycopg2.connect(**MENTORING)
    cur = conn.cursor()
    last_run = get_last_run("orgMentorRatings")

    if last_run is None:
        cur.execute("""
            SELECT ue.user_id, ue.rating, ue.updated_at, ue.organization_id, ue.tenant_code, ue.deleted_at, oe.name
            FROM user_extensions ue
            LEFT JOIN organization_extension oe ON ue.organization_id = oe.organization_id
            WHERE ue.rating IS NOT NULL AND ue.is_mentor = TRUE
        """)
    else:
        cur.execute("""
            SELECT ue.user_id, ue.rating, ue.updated_at, ue.organization_id, ue.tenant_code, ue.deleted_at, oe.name
            FROM user_extensions ue
            LEFT JOIN organization_extension oe ON ue.organization_id = oe.organization_id
            WHERE ue.rating IS NOT NULL AND ue.is_mentor = TRUE
              AND ue.updated_at > %s
        """, (last_run,))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} mentor ratings")

    for row in rows:
        user_id, rating, updated_at, organization_id, tenant_code, deleted_at, name = row
        avg_rating = rating.get("average") if isinstance(rating, dict) else rating

        event = {
            "eventType": "create",
            "entity": "rating",
            "tenant_code": tenant_code,
            "org_id": organization_id,
            "org_name": name,
            "mentor_id": user_id,
            "rating": avg_rating,
            "rating_updated_at": updated_at,
            "deleted": deleted_at is not None
        }
        push_event(event)
        producer.flush()

    update_last_run("orgMentorRatings", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Main ----------------
def main():
    process_sessions()
    process_attendance()
    process_connections()
    process_connection_requests()
    process_orgMentorRatings()

if __name__ == "__main__":
    main()
