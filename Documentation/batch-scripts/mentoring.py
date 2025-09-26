import os, json, psycopg2, logging
from kafka import KafkaProducer
from datetime import datetime, timedelta
from collections import defaultdict
import json as pyjson
from datetime import datetime, timedelta, timezone
import configparser


# Load config.ini
config = configparser.ConfigParser()
config.read("config.ini")

# --- DB Configs ---
SOURCE_DB1 = dict(config["SOURCE_DB1"])
SOURCE_DB2 = dict(config["SOURCE_DB2"])

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
    producer.send(TOPIC, event)

# ---------------- Logging Setup ----------------
LOG_DIR = "logs"
os.makedirs(LOG_DIR, exist_ok=True)

log_file = os.path.join(LOG_DIR, f"batch_run_{datetime.now(timezone.utc).strftime('%Y%m%d_%H%M%S')}.log")

logging.basicConfig(
    filename=log_file,
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)

# Cleanup logs older than 7 days
for f in os.listdir(LOG_DIR):
    fpath = os.path.join(LOG_DIR, f)
    if os.path.isfile(fpath):
        mtime = datetime.fromtimestamp(os.path.getmtime(fpath), timezone.utc)
        if mtime < datetime.now(timezone.utc) - timedelta(days=7):
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
    conn = psycopg2.connect(**SOURCE_DB1)
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
            "deleted": False
        }
        push_event(event)
        producer.flush()

    update_last_run("sessions", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Attendance ----------------
def process_attendance():
    logging.info("Processing attendance...")
    conn = psycopg2.connect(**SOURCE_DB1)
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
            "deleted": False
        }
        push_event(event)
        producer.flush()

    update_last_run("attendance", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Connections ----------------
def process_connections():
    logging.info("Processing connections...")
    conn = psycopg2.connect(**SOURCE_DB1)
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
            "deleted": False
        }
        push_event(event)
        producer.flush()

    update_last_run("connections", datetime.now(timezone.utc))

    cur.close()
    conn.close()

# ---------------- Org Mentor Ratings ----------------
def process_orgMentorRatings():
    logging.info("Processing org mentor ratings...")
    conn = psycopg2.connect(**SOURCE_DB1)
    cur = conn.cursor()
    last_run = get_last_run("orgMentorRatings")

    if last_run is None:
        cur.execute("""
            SELECT ue.user_id, ue.rating, ue.updated_at, ue.organization_id, ue.tenant_code, oe.name
            FROM user_extensions ue
            LEFT JOIN organization_extension oe ON ue.organization_id = oe.organization_id
            WHERE ue.rating IS NOT NULL AND ue.is_mentor = TRUE
        """)
    else:
        cur.execute("""
            SELECT ue.user_id, ue.rating, ue.updated_at, ue.organization_id, ue.tenant_code, oe.name
            FROM user_extensions ue
            LEFT JOIN organization_extension oe ON ue.organization_id = oe.organization_id
            WHERE ue.rating IS NOT NULL AND ue.is_mentor = TRUE
              AND ue.updated_at > %s
        """, (last_run,))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} mentor ratings")

    for row in rows:
        user_id, rating, updated_at, organization_id, tenant_code, name = row
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
            "deleted": False
        }
        push_event(event)
        producer.flush()

    update_last_run("orgMentorRatings", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Org Roles ----------------
def process_orgRoles():
    logging.info("Processing org roles...")
    conn = psycopg2.connect(**SOURCE_DB2)
    cur = conn.cursor()
    last_run = get_last_run("orgRoles")

    if last_run is None:
        cur.execute("""
            SELECT
                uor.user_id,
                o.id AS org_id,
                o.name AS org_name,
                o.code AS org_code,
                r.id AS role_id,
                r.title AS role_name,
                uor.tenant_code,
                uor.updated_at
            FROM user_organization_roles uor
            JOIN organizations o
                ON uor.tenant_code = o.tenant_code
               AND uor.organization_code = o.code
            JOIN user_roles r
                ON uor.role_id = r.id
               AND r.organization_id = o.id
        """)
    else:
        cur.execute("""
            SELECT
                uor.user_id,
                o.id AS org_id,
                o.name AS org_name,
                o.code AS org_code,
                r.id AS role_id,
                r.title AS role_name,
                uor.tenant_code,
                uor.updated_at
            FROM user_organization_roles uor
            JOIN organizations o
                ON uor.tenant_code = o.tenant_code
               AND uor.organization_code = o.code
            JOIN user_roles r
                ON uor.role_id = r.id
               AND r.organization_id = o.id
            WHERE uor.updated_at > %s
        """, (last_run,))

    rows = cur.fetchall()
    logging.info(f"Fetched {len(rows)} org roles")

    users = defaultdict(lambda: {"organizations": defaultdict(list)})

    for row in rows:
        user_id, org_id, org_name, org_code, role_id, role_name, tenant_code, updated_at = row
        user_entry = users[(user_id, tenant_code)]
        orgs = user_entry["organizations"]
        orgs[org_id].append({
            "id": role_id,
            "title": role_name,
            "label": role_name
        })
        user_entry["userId"] = user_id
        user_entry["tenant_code"] = tenant_code
        user_entry["org_meta"] = user_entry.get("org_meta", {})
        user_entry["org_meta"][org_id] = {
            "id": org_id,
            "name": org_name,
            "code": org_code
        }

    for (user_id, tenant_code), data in users.items():
        orgs_list = []
        for org_id, roles in data["organizations"].items():
            org_meta = data["org_meta"][org_id]
            orgs_list.append({
                "id": org_meta["id"],
                "name": org_meta["name"],
                "code": org_meta["code"],
                "roles": roles
            })

        event = {
            "eventType": "create",
            "entity": "orgRoles",
            "userId": data["userId"],
            "tenant_code": data["tenant_code"],
            "organizations": orgs_list,
            "status": "ACTIVE",
            "deleted": False
        }
        push_event(event)
        producer.flush()

    update_last_run("orgRoles", datetime.now(timezone.utc))
    cur.close()
    conn.close()

# ---------------- Main ----------------
def main():
    process_sessions()
    process_attendance()
    process_connections()
    process_orgMentorRatings()
    process_orgRoles()

if __name__ == "__main__":
    main()
