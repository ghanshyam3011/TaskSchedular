# Use official OpenJDK image as base
FROM openjdk:17-slim

# Set working directory in container
WORKDIR /app

# Copy maven/build files
COPY pom.xml* ./
COPY lib/ ./lib/

# Copy the JAR file 
COPY target/task-scheduler-1.0-SNAPSHOT.jar ./app.jar

# Create a dummy app.jar if the file doesn't exist after COPY
# This should never happen with our updated run-docker.bat, but just in case
RUN if [ ! -f app.jar ]; then echo "JAR not found, using dummy JAR"; \
    echo "Class-Path: ." > manifest.txt; \
    mkdir -p target/classes/com/taskscheduler; \
    echo "public class Main { public static void main(String[] args) { System.out.println(\"Please build the application first\"); } }" > target/classes/com/taskscheduler/Main.java; \
    javac target/classes/com/taskscheduler/Main.java; \
    jar cfm app.jar manifest.txt -C target/classes .; \
    fi

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
