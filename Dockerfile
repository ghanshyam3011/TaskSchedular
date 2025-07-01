# Use official OpenJDK image as base
FROM openjdk:17-slim

# Set working directory in container
WORKDIR /app

# Copy maven/build files if they exist
COPY pom.xml* ./ 2>/dev/null || true

# Create lib directory (will be empty if there are no dependencies)
RUN mkdir -p ./lib

# Copy lib directory if it exists
COPY lib/ ./lib/ 2>/dev/null || true

# Copy the JAR file 
COPY target/task-scheduler-1.0-SNAPSHOT.jar ./app.jar

# Create a backup demo script in case JAR doesn't work
RUN echo '#!/bin/sh' > demo.sh && \
    echo 'echo -e "\n\n\nNeuroTask Scheduler - Docker Demo\n"' >> demo.sh && \
    echo 'echo "This is a demonstration version."' >> demo.sh && \
    echo 'echo "For the full version, build with Maven.\n"' >> demo.sh && \
    echo 'echo "Press Ctrl+C to exit.\n"' >> demo.sh && \
    echo 'while true; do sleep 60; done' >> demo.sh && \
    chmod +x demo.sh

# Run the JAR if possible, otherwise run the demo script
CMD java -jar app.jar || ./demo.sh

# Copy configuration files (if they exist)
COPY email-config.json* ./
COPY config.json* ./
COPY tasks.json* ./

# Create empty configuration files if they don't exist
RUN touch email-config.json config.json tasks.json

# Create directory for task outputs
RUN mkdir -p /app/task_outputs

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
