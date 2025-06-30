# Use official OpenJDK image as base
FROM openjdk:17-slim

# Set working directory in container
WORKDIR /app

# Copy maven/build files
COPY pom.xml .
COPY lib/ ./lib/
COPY target/task-scheduler-1.0-SNAPSHOT.jar ./app.jar

# Copy configuration files
COPY email-config.json .
COPY config.json .
COPY tasks.json .

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
