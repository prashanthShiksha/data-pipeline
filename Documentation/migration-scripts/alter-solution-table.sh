#!/usr/bin/env bash
# Logs
export LOG_FILE="/home/user2/Documents/elevate-data/data-pipeline/Documentation/migration-scripts/orgid_update.log"

# Postgres Config
export PGHOST="localhost"
export PGPORT="5432"
export PGDBNAME="test"
export PGUSER="postgres"
export PGPASSWORD="postgres"

# Programs DB Find API Config
export API_URL="https://qa.elevate-apis.shikshalokam.org/project/v1/admin/dbFind/programs"
export AUTH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoxLCJuYW1lIjoiTmV2aWwiLCJzZXNzaW9uX2lkIjoxOTYyNywib3JnYW5pemF0aW9uX2lkcyI6WyIxIl0sIm9yZ2FuaXphdGlvbl9jb2RlcyI6WyJkZWZhdWx0X2NvZGUiXSwidGVuYW50X2NvZGUiOiJkZWZhdWx0Iiwib3JnYW5pemF0aW9ucyI6W3siaWQiOjEsIm5hbWUiOiJEZWZhdWx0IE9yZ2FuaXphdGlvbiIsImNvZGUiOiJkZWZhdWx0X2NvZGUiLCJkZXNjcmlwdGlvbiI6IkRlZmF1bHQgIFNMIE9yZ2FuaXNhdGlvbiIsInN0YXR1cyI6IkFDVElWRSIsInJlbGF0ZWRfb3JncyI6bnVsbCwidGVuYW50X2NvZGUiOiJkZWZhdWx0IiwibWV0YSI6bnVsbCwiY3JlYXRlZF9ieSI6bnVsbCwidXBkYXRlZF9ieSI6MSwicm9sZXMiOlt7ImlkIjo1LCJ0aXRsZSI6Im1lbnRlZSIsImxhYmVsIjpudWxsLCJ1c2VyX3R5cGUiOjAsInN0YXR1cyI6IkFDVElWRSIsIm9yZ2FuaXphdGlvbl9pZCI6MSwidmlzaWJpbGl0eSI6IlBVQkxJQyIsInRlbmFudF9jb2RlIjoiZGVmYXVsdCIsInRyYW5zbGF0aW9ucyI6bnVsbH0seyJpZCI6MjUsInRpdGxlIjoibGVhcm5lciIsImxhYmVsIjoiTGVhcm5lciIsInVzZXJfdHlwZSI6MCwic3RhdHVzIjoiQUNUSVZFIiwib3JnYW5pemF0aW9uX2lkIjoxLCJ2aXNpYmlsaXR5IjoiUFVCTElDIiwidGVuYW50X2NvZGUiOiJkZWZhdWx0IiwidHJhbnNsYXRpb25zIjpudWxsfV19XX0sImlhdCI6MTc2MTQ4MTczOSwiZXhwIjoxNzYxNTY4MTM5fQ.u2P0gq240T1YgdRU6QcJgJSODp0DJ_YbB87U4WPrWfE"   # set via environment or secret manager
export APP_NAME="elevatedata"

# Table Names
export SOLUTION_TABLE="local_solutions"

# --- Logging setup ---
mkdir -p "$(dirname "$LOG_FILE")"
touch "$LOG_FILE"
log() { local m="$1"; printf '%s - %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$m" | tee -a "$LOG_FILE"; }

trap 'log "Script exited with code $?."' EXIT

PG_CONN="postgresql://$PGUSER:$PGPASSWORD@$PGHOST:$PGPORT/$PGDBNAME"

log "📌 Checking if org_id column exists in ${SOLUTION_TABLE}..."
if ! psql "$PG_CONN" -t -A -c \
  "SELECT 1 FROM information_schema.columns WHERE table_name='${SOLUTION_TABLE}' AND column_name='org_id';" \
  | grep -q 1; then
  log "🛠️ Adding org_id column to ${SOLUTION_TABLE}"
  psql "$PG_CONN" -v ON_ERROR_STOP=1 -c "ALTER TABLE \"${SOLUTION_TABLE}\" ADD COLUMN org_id TEXT;" \
    && log "✅ org_id column added successfully" || { log "❌ Failed to add org_id column"; exit 1; }
else
  log "ℹ️ org_id column already exists"
fi

log "📦 Fetching distinct program_ids from ${SOLUTION_TABLE}..."
# Read results into an array, one program_id per line
mapfile -t program_ids < <(psql "$PG_CONN" -t -A -v ON_ERROR_STOP=1 -c "SELECT DISTINCT program_id FROM ${SOLUTION_TABLE} WHERE program_id IS NOT NULL;")

if [ "${#program_ids[@]}" -eq 0 ]; then
  log "❌ No program_ids found!"
  exit 0
fi

log "✅ Found ${#program_ids[@]} program_ids to process."

for program_id in "${program_ids[@]}"; do
  log "🔍 Processing program_id: $program_id"

  response=$(curl -sS --location "$API_URL" \
    --header "x-auth-token: $AUTH_TOKEN" \
    --header "appname: $APP_NAME" \
    --header "Content-Type: application/json" \
    --data "{\"query\":{\"_id\":\"$program_id\"},\"sort\":{\"createdAt\":\"-1\"},\"mongoIdKeys\":[\"_id\"]}" ) || {
      log "❌ curl failed for program_id: $program_id"
      continue
  }

  orgId=$(echo "$response" | jq -r '.result[0].orgId // empty' || true)

  if [ -z "$orgId" ] || [ "$orgId" = "null" ]; then
    log "⚠️  No orgId found for program_id: $program_id"
    continue
  fi

  # Escape single quotes for SQL literal
  esc_orgId=${orgId//\'/\'\'}
  esc_program_id=${program_id//\'/\'\'}

  # Update using psql; fail on error
  if psql "$PG_CONN" -v ON_ERROR_STOP=1 -c "UPDATE ${SOLUTION_TABLE} SET org_id = '${esc_orgId}' WHERE program_id = '${esc_program_id}';" >/dev/null; then
    log "✅ Updated org_id=$orgId for program_id=$program_id"
  else
    log "❌ Failed to update for program_id=$program_id"
  fi
done

log "🎯 All program_ids processed."
