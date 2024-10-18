#!/bin/bash

## This script retrieves a Metabase dashboard's existing parameters using a session token and dashboard ID,
## appends new filtering parameters to the existing ones, and updates the dashboard with the modified parameters.

# ANSI escape codes for colors
BOLD_YELLOW="\033[1;33m"
NC="\033[0m"

echo -e "${BOLD_YELLOW}           :: Update the dashboard with new parameters ::           ${NC}"
echo -e "${NC}"

# External Path
report_path="$1"
main_dir_path="$2"
parameter_value="$3"
echo "$parameter_value"

# Check if metadata_file.txt exists
METADATA_FILE="$main_dir_path/metadata_file.txt"
if [ ! -f "$METADATA_FILE" ]; then
    echo "Error: metadata_file.txt not found."
    exit 1
fi

# Read SESSION_TOKEN, DASHBOARD_ID, and METABASE_URL from metadata_file.txt
SESSION_TOKEN=$(grep "SESSION_TOKEN" "$METADATA_FILE" | cut -d ' ' -f 2)
DASHBOARD_ID=$(grep "DASHBOARD_ID" "$METADATA_FILE" | cut -d ' ' -f 2)
METABASE_URL=$(grep "METABASE_URL" "$METADATA_FILE" | cut -d ' ' -f 2)

# Parameter details to add (with $parameter_value correctly replaced)
PARAMETER_JSON=$(jq -n \
    --arg program_name "$parameter_value" \
    '[
        {
            "slug": "select_program_name",
            "default": [$program_name],
            "name": "Select Program Name",
            "isMultiSelect": false,
            "type": "string/=",
            "sectionId": "string",
            "values_source_type": "static-list",
            "id": "8c7d86ea",
            "values_source_config": {
                "values": [$program_name]
            },
            "required": true
        },
        {
            "slug": "select_state",
            "name": "Select State",
            "isMultiSelect": false,
            "type": "string/=",
            "sectionId": "location",
            "id": "c32c8fc5",
            "required": false,
            "filteringParameters": [
                "8c7d86ea"
            ]
        },
        {
            "slug": "select_district",
            "filteringParameters": [
                "c32c8fc5",
                "8c7d86ea"
            ],
            "name": "Select District",
            "isMultiSelect": false,
            "type": "string/=",
            "sectionId": "location",
            "id": "74a10335"
        }
    ]'
)

# Get the existing dashboard details
dashboard_response=$(curl --silent --location --request GET "$METABASE_URL/api/dashboard/$DASHBOARD_ID" \
--header "Content-Type: application/json" \
--header "X-Metabase-Session: $SESSION_TOKEN")

# Extract the current parameters from the dashboard
current_parameters=$(echo "$dashboard_response" | jq -r '.parameters')

# Add the new parameter to the dashboard's existing parameters
updated_parameters=$(echo "$current_parameters" | jq -r ". + $PARAMETER_JSON")

# Update the dashboard with the new parameters
update_response=$(curl --silent --location --request PUT "$METABASE_URL/api/dashboard/$DASHBOARD_ID" \
--header "Content-Type: application/json" \
--header "X-Metabase-Session: $SESSION_TOKEN" \
--data-raw "{
    \"parameters\": $updated_parameters
}")

# Check if the dashboard was updated successfully
if echo "$update_response" | grep -q '"parameters"'; then
    echo ">>  Dashboard parameters updated successfully."
else
    echo "Error: Failed to update dashboard parameters. Response: $update_response"
    exit 1
fi

echo ">>  [05_add_parameters.sh] Script executed successfully!"
