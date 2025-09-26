#!/bin/bash
source /etc/profile
export TZ=Asia/Kolkata
date
echo "RUNNING JOB"

echo ""
echo "$(date)"
echo "====================================="
echo "Batch script has triggered"
. /home/user2/Documents/elevate-new/data-pipeline/myvenv/bin/activate && python3 /home/user2/Documents/elevate-new/data-pipeline/Documentation/batch-scripts/mentoring.py
echo "Batch scripts has finished"
echo "*************************************"