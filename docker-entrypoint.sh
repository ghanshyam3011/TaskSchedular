#!/bin/sh
set -e

# Ensure the task outputs directory exists and is writable
mkdir -p /app/task_outputs
chmod 777 /app/task_outputs

# Use bash for command execution while in Docker
echo "# Docker detection file - tells the app to use bash instead of cmd.exe" > /app/running_in_docker
chmod 644 /app/running_in_docker

# Execute the Java application
exec java -jar app.jar "$@"
