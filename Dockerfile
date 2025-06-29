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

# Expose any ports needed (if your app uses a web interface in future)
# EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command (can be overridden at runtime)
CMD ["--foreground"]
