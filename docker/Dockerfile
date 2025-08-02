FROM openjdk:17-slim

WORKDIR /app

# Create app directories
RUN mkdir -p ./lib /app/task_outputs
RUN touch email-config.json config.json tasks.json

# Copy application files
COPY target/task-scheduler-1.0-SNAPSHOT.jar ./app.jar
COPY *.json ./

# Set environment variables
ENV BACKGROUND_MODE=false
ENV RUNNING_IN_DOCKER=true

# Create entrypoint script
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'mkdir -p /app/task_outputs' >> /app/entrypoint.sh && \
    echo 'chmod 777 /app/task_outputs' >> /app/entrypoint.sh && \
    echo 'echo "# Docker detection file" > /app/running_in_docker' >> /app/entrypoint.sh && \
    echo 'chmod 644 /app/running_in_docker' >> /app/entrypoint.sh && \
    echo 'java -jar app.jar "$@"' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["--foreground"]
