#!/bin/sh
# Docker entrypoint script

# Setup directories and Docker detection file
mkdir -p /app/task_outputs
chmod 777 /app/task_outputs
echo "# Docker detection file" > /app/running_in_docker
chmod 644 /app/running_in_docker

# Run application
java -jar app.jar "$@"
