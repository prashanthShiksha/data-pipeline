#!/bin/bash
source /etc/profile
export TZ=Asia/Kolkata
date

VENV_PATH="/home/user2/Documents/elevate-new/data-pipeline/myvenv"
SCRIPT_PATH="/home/user2/Documents/elevate-new/data-pipeline/Documentation/batch-scripts/mentoring.py"

echo "RUNNING JOB"

echo ""
echo "$(date)"
echo "====================================="
echo "Python Batch script has triggered"

# Correct variable usage with $
. "$VENV_PATH/bin/activate" && python3 "$SCRIPT_PATH"

echo "Python Batch script has finished"
echo "*************************************"
