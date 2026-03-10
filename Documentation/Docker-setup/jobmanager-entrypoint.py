# TODO: up akka service + data cleanup + mentoring batch job + autosubmission of flink job (need based as per requirement (pass as a parameter (yes/no)))
import os
import sys
import json
import time
import base64
import zipfile
import subprocess
import atexit
import requests
from pyhocon import ConfigFactory

print("Starting Elevate Data Flink Job Runner...")

# ---------------------------------------------------
# Load configuration
# ---------------------------------------------------
UNIFIED_CONF = os.getenv("UNIFIED_PIPELINE_CONF", "/home/user2/Documents/elevate-data-optimised/data-pipeline/unified-pipeline.conf")

if not os.path.exists(UNIFIED_CONF):
    print(f"ERROR: unified-pipeline.conf not found at {UNIFIED_CONF}")
    sys.exit(1)

# ---------------------------------------------------
# Load HOCON configuration
# ---------------------------------------------------

conf = ConfigFactory.parse_file(UNIFIED_CONF)
hc = conf["health-check"]

HEALTH_API_URL = hc["api-url"]
AUTH_TOKEN = hc["auth-token"]
CHECK_INTERVAL_SEC = hc["check-interval-sec"]
FLINK_URL = hc["flink-url"]

JOB_JARS = dict(hc["job-jars"])
JOB_CONF_ARRAY = list(hc["job-conf"])

# Akka-service config
_as = conf["akka-service"]
AKKA_JAR     = os.getenv("akka-jar", "/home/user2/Documents/elevate-data-optimised/data-pipeline/akka-service/target/akka-service-1.0.0.jar")
AKKA_HOST    = _as.get("akka.http.host", "localhost")
AKKA_PORT    = int(_as.get("akka.http.port", 8080))
AKKA_HEALTH  = f"http://{AKKA_HOST}:{AKKA_PORT}/api/health"
AKKA_TOKEN   = _as["security"]["api-token"]

# Data-cleanup config
_dc = conf.get("data-cleanup", {})
DATA_CLEANUP_ENABLED = str(_dc.get("enabled", "no")).lower() == "yes"
CLEANUP_SCRIPT = os.getenv("CLEANUP_SCRIPT", "/app/Documentation/data-cleanup/python-script/resource_delete.py")

# Migration-push config
_mp = conf.get("migration-push", {})
MIGRATION_PUSH_ENABLED = str(_mp.get("enabled", "no")).lower() == "yes"
MIGRATION_SCRIPT = os.getenv("MIGRATION_SCRIPT", "/app/Documentation/migration-scripts/python-scripts/push_kafka_messages.py")

# ---------------------------------------------------
# Akka Service
# ---------------------------------------------------

_akka_proc = None

def start_akka_service():
    """Start the akka-service JAR as a background subprocess."""
    global _akka_proc

    if not os.path.exists(AKKA_JAR):
        print(f"ERROR: Akka service JAR not found at {AKKA_JAR}")
        sys.exit(1)

    print(f"Starting akka-service from {AKKA_JAR} ...")

    _akka_proc = subprocess.Popen(
        ["java", "-jar", AKKA_JAR],
        env={**os.environ, "UNIFIED_PIPELINE_CONF": UNIFIED_CONF},
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(f"Akka service started (PID={_akka_proc.pid})")


def wait_for_akka(retries=30, delay=3):
    """Poll the lightweight /api/ping endpoint until it returns 200."""
    ping_url = f"http://{AKKA_HOST}:{AKKA_PORT}/api/ping"
    print(f"Waiting for akka-service to become ready ({ping_url})...")

    for attempt in range(1, retries + 1):
        try:
            r = requests.get(ping_url, timeout=5)
            if r.status_code == 200:
                print(f"Akka service is ready (attempt {attempt}).")
                return
        except Exception:
            pass

        # Check if process has died unexpectedly
        if _akka_proc and _akka_proc.poll() is not None:
            print("ERROR: Akka service process has exited unexpectedly.")
            sys.exit(1)

        print(f"Akka not ready yet, retrying ({attempt}/{retries})...")
        time.sleep(delay)

    print("ERROR: Akka service did not become ready in time.")
    sys.exit(1)


def stop_akka_service():
    """Gracefully terminate the akka-service subprocess on exit."""
    if _akka_proc and _akka_proc.poll() is None:
        print("Stopping akka-service...")
        _akka_proc.terminate()
        try:
            _akka_proc.wait(timeout=10)
            print("Akka service stopped.")
        except subprocess.TimeoutExpired:
            print("Akka service did not stop cleanly, killing it.")
            _akka_proc.kill()

atexit.register(stop_akka_service)

# ---------------------------------------------------
# Data Cleanup
# ---------------------------------------------------

def setup_data_cleanup():
    """Setup and start the data cleanup script in a tmux session if enabled."""
    if not DATA_CLEANUP_ENABLED:
        print("Data cleanup is disabled in config. Skipping setup.")
        return

    print("Setting up data cleanup...")

    # 1. Check if tmux is installed
    try:
        subprocess.run(["tmux", "-V"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("ERROR: tmux not found. Data cleanup requires tmux to run in the background.")
        print("Please install tmux manually (e.g., sudo apt install tmux -y). Skipping cleanup setup.")
        return

    # 2. Identify the script path
    actual_script = CLEANUP_SCRIPT
    if not os.path.exists(actual_script) and actual_script.startswith("/app/"):
        # Resolve relative to the local checkout (same logic as submit_job)
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        local_path = actual_script.replace("/app", base_dir)
        if os.path.exists(local_path):
            actual_script = local_path

    if not os.path.exists(actual_script):
        print(f"ERROR: Resource cleanup script not found at {actual_script}")
        return

    # 3. Check if session already exists
    res = subprocess.run(["tmux", "has-session", "-t", "resource_cleanup"], capture_output=True)
    if res.returncode == 0:
        print("tmux session 'resource_cleanup' already exists. Killing it to restart.")
        subprocess.run(["tmux", "kill-session", "-t", "resource_cleanup"])

    # 4. Create a new detached tmux session and run the script
    print(f"Starting data cleanup script in tmux session 'resource_cleanup': {actual_script}")
    # Use full path for python3 as recommended
    cmd = f"python3 {actual_script}"
    subprocess.run(["tmux", "new-session", "-d", "-s", "resource_cleanup", cmd])
    print("Data cleanup setup complete and session started in background.")


def setup_migration_push():
    """Setup and start the migration push script in a tmux session if enabled."""
    if not MIGRATION_PUSH_ENABLED:
        print("Migration push is disabled in config. Skipping setup.")
        return

    print("Setting up migration push...")

    # 1. Check if tmux is installed
    try:
        subprocess.run(["tmux", "-V"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("ERROR: tmux not found. Migration push requires tmux to run in the background.")
        print("Please install tmux manually (e.g., sudo apt install tmux -y). Skipping migration setup.")
        return

    # 2. Identify the script path
    actual_script = MIGRATION_SCRIPT
    if not os.path.exists(actual_script) and actual_script.startswith("/app/"):
        # Resolve relative to the local checkout
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        local_path = actual_script.replace("/app", base_dir)
        if os.path.exists(local_path):
            actual_script = local_path

    if not os.path.exists(actual_script):
        print(f"ERROR: Migration push script not found at {actual_script}")
        return

    # 3. Check if session already exists
    res = subprocess.run(["tmux", "has-session", "-t", "migration_push"], capture_output=True)
    if res.returncode == 0:
        print("tmux session 'migration_push' already exists. Killing it to restart.")
        subprocess.run(["tmux", "kill-session", "-t", "migration_push"])

    # 4. Create a new detached tmux session and run the script
    print(f"Starting migration push script in tmux session 'migration_push': {actual_script}")
    cmd = f"python3 {actual_script}"
    subprocess.run(["tmux", "new-session", "-d", "-s", "migration_push", cmd])
    print("Migration push setup complete and session started in background.")


# ---------------------------------------------------
# Helpers
# ---------------------------------------------------

def get_entry_class_from_jar(jar_path):
    """Extract Main-Class from JAR manifest, handling multiline wrapping"""
    try:
        with zipfile.ZipFile(jar_path) as jar:
            manifest = jar.read("META-INF/MANIFEST.MF").decode()

            # Manifest lines can wrap at 72 chars; continuation lines start with a space.
            lines = []
            for line in manifest.splitlines():
                if line.startswith(" "):
                    if lines:
                        lines[-1] += line[1:]
                elif line.strip():
                    lines.append(line)

            for line in lines:
                if line.startswith("Main-Class:"):
                    return line.split(":", 1)[1].strip()
    except Exception as e:
        print(f"Failed reading manifest: {e}")

    return None


def get_config_b64(conf_file):
    """Read config file and return base64 encoded content. Resolves /app paths locally."""
    actual_path = conf_file
    if not os.path.exists(actual_path) and actual_path.startswith("/app/"):
        # Try to resolve relative to the local checkout
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        local_path = actual_path.replace("/app", base_dir)
        if os.path.exists(local_path):
            actual_path = local_path

    with open(actual_path, "rb") as f:
        return base64.b64encode(f.read()).decode()


def wait_for_flink():
    print("Waiting for Flink JobManager...")

    for _ in range(30):
        try:
            r = requests.get(f"{FLINK_URL}/overview", timeout=5)
            if r.status_code == 200:
                print("Flink JobManager is ready.")
                return
        except:
            pass

        print("Waiting...")
        time.sleep(2)

    print("Flink JobManager did not start.")
    sys.exit(1)


def upload_jar(jar_path):

    with open(jar_path, "rb") as f:

        files = {"jarfile": f}

        r = requests.post(
            f"{FLINK_URL}/jars/upload",
            files=files
        )

    data = r.json()

    filename = data.get("filename", "")

    return filename.split("/")[-1]


def submit_job(jar, conf):
    """Submit a Flink job. Resolves /app paths locally."""
    actual_jar = jar
    if not os.path.exists(actual_jar) and actual_jar.startswith("/app/"):
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        local_jar = actual_jar.replace("/app", base_dir)
        if os.path.exists(local_jar):
            actual_jar = local_jar

    config_b64 = get_config_b64(conf)

    print("----------------------------------------")
    print("Submitting Job")
    print("Jar  :", actual_jar)
    print("Conf :", conf)
    print("----------------------------------------")

    entry_class = get_entry_class_from_jar(actual_jar)

    print("Detected Entry Class:", entry_class)

    if not entry_class:
        print("ERROR: Could not detect entry class.")
        return

    jar_id = upload_jar(actual_jar)

    if not jar_id:
        print("Jar upload failed")
        return

    print("Uploaded jar id:", jar_id)

    payload = {
        "entryClass": entry_class,
        "programArgs": f"--config.content {config_b64}"
    }

    requests.post(
        f"{FLINK_URL}/jars/{jar_id}/run",
        json=payload
    )

    print("Job submitted.")


def check_api_job_status(job_name):

    headers = {"Authorization": AUTH_TOKEN}

    r = requests.get(
        HEALTH_API_URL,
        headers=headers
    )

    data = r.json()
    # FlinkHealth response is { "cluster": ..., "jobs": [...] }
    jobs = data.get("jobs", []) if isinstance(data, dict) else []

    for job in jobs:
        if isinstance(job, dict) and job.get("name") == job_name and job.get("status") == "RUNNING":
            return True

    return False


# ---------------------------------------------------
# Job monitor loop
# ---------------------------------------------------

def monitor_jobs():

    conf = JOB_CONF_ARRAY[0]

    while True:

        print("Checking Flink jobs...")

        for name, jar in JOB_JARS.items():

            if check_api_job_status(name):

                print(f"Job '{name}' is already running.")

            else:

                print(f"Submitting job '{name}'...")

                submit_job(jar, conf)

        print(f"Sleeping {CHECK_INTERVAL_SEC} seconds...")

        time.sleep(CHECK_INTERVAL_SEC)


# ---------------------------------------------------
# Start
# ---------------------------------------------------

if __name__ == "__main__":

    # 1. Start akka-service
    start_akka_service()
    wait_for_akka()

    # 2. Wait for Flink JobManager
    wait_for_flink()

    # 3. Setup data cleanup (if enabled)
    setup_data_cleanup()

    # 4. Setup migration push (if enabled)
    setup_migration_push()

    # 5. Start Flink job monitor loop
    monitor_jobs()