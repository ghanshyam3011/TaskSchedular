# Use official OpenJDK image as base
FROM openjdk:17-slim

# Set working directory in container
WORKDIR /app

# Create necessary directories
RUN mkdir -p ./lib
RUN mkdir -p /app/task_outputs

# Create empty configuration files to ensure they exist
RUN touch email-config.json config.json tasks.json

# Copy the JAR file (this must exist)
COPY target/task-scheduler-1.0-SNAPSHOT.jar ./app.jar

# Copy optional configuration files if they exist (wildcard patterns will not fail)
COPY *.json ./

# Create a backup demo script in case JAR doesn't work
RUN echo '#!/bin/sh' > demo.sh && \
    echo 'echo -e "\n\n\nNeuroTask Scheduler - Docker Demo\n"' >> demo.sh && \
    echo 'echo "This is a demonstration version."' >> demo.sh && \
    echo 'echo "For the full version, build with Maven.\n"' >> demo.sh && \
    echo 'echo "Press Ctrl+C to exit.\n"' >> demo.sh && \
    echo 'while true; do sleep 60; done' >> demo.sh && \
    chmod +x demo.sh

# Make sure we run in foreground mode in container
ENV BACKGROUND_MODE=false

# Make sure the container knows it's running in Docker
ENV RUNNING_IN_DOCKER=true

# Expose any ports needed (if your app uses a web interface in future)
# EXPOSE 8080

# Copy and set up entrypoint script
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

# Set the entrypoint
ENTRYPOINT ["/app/docker-entrypoint.sh"]

# Default command (can be overridden at runtime)
CMD ["--foreground"]
