📦 Batch Scripts – Mentoring Events (Phase 1)

This module provides batch processing scripts to generate Mentoring Service events when real-time Kafka events are not available.
The scripts query PostgreSQL databases, build event JSONs, and publish them to Kafka.

⚙️ What the Scripts Do
🔹 mentoring.py

Connects to Mentoring DB and Tenant DB.

Extracts and processes data from multiple sources:

OrgMentorRating → Reads user_extensions (where rating IS NOT NULL and is_mentor = true) and computes average ratings.

Sessions → Reads sessions table and builds session events (session_id, mentor_id, title, status, started_at, completed_at, etc.).

Session Attendance → Reads session_attendees and builds attendance events (attendance_id, session_id, mentee_id, joined_at, left_at, etc.).

Connections → Reads connections and builds connection events (connection_id, user_id, friend_id, status, timestamps).

OrgRoles → Joins data across user_roles, user_organization, and organizations to build role events.

Publishes JSON events to Kafka with event types:

*mentoring-sessions*

*mentoring-attendance*

*mentoring-connection*

*mentoring-orgroles*

🔹 trigger-batch-script.sh

Helper wrapper that:

Creates a Python virtual environment (if missing).

Installs dependencies from requirements.txt.

Executes mentoring.py.

🚀 How to Run
1️⃣ Run with Python (manual way)
# Navigate to batch-scripts directory
cd batch-scripts

# Create virtual environment (first time only)
python -m venv venv

# Activate venv
# Linux/Mac:
source venv/bin/activate
# Windows (PowerShell):
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run script
python mentoring.py


✅ Logs will be generated in the logs/ folder.

2️⃣ Run with Shell Script (automated way)
cd batch-scripts

# Make executable (first time only)
chmod +x trigger-batch-script.sh

# Run the script
./trigger-batch-script.sh


This script ensures:

Virtual environment is created (if missing).

Dependencies are installed.

mentoring.py runs automatically.