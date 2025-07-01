@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Building and running NeuroTask in Docker...
echo.

REM First, make sure the JAR is built
echo Building Java application...
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
  echo Maven build is required...
  if exist ".\mvnw.cmd" (
    call .\mvnw.cmd clean package -DskipTests
  ) else (
    echo Maven wrapper not found. Please build the application first with:
    echo mvn clean package -DskipTests
    exit /b 1
  )
)

REM Stop and remove any existing container
docker stop neurotask 2>nul
docker rm neurotask 2>nul

REM Build the image
echo Building Docker image...
docker build -t neurotask .

REM Run the container in interactive mode
echo Starting application in Docker container...
echo.
docker run -it --name neurotask --rm neurotask

echo.
echo Container stopped.
pause
