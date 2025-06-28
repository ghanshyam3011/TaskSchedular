@echo off
echo Building and running Task Scheduler application...

REM Check if JAR exists
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
    echo JAR file not found. Building the application...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo Failed to build the application. See errors above.
        pause
        exit /b 1
    )
)

echo Starting Task Scheduler...
java -jar target\task-scheduler-1.0-SNAPSHOT.jar

pause
