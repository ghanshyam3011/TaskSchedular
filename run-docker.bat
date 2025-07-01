@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Building and running NeuroTask in Docker...
echo.

REM Check if JAR exists
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
  echo JAR file not found. Downloading prebuilt JAR...
  
  REM Create target directory if it doesn't exist
  if not exist "target" mkdir target
  
  REM Use curl to download the JAR file (curl is included in Windows 10+)
  curl -L -o target\task-scheduler-1.0-SNAPSHOT.jar https://github.com/ghanshyam3011/TaskSchedular/releases/latest/download/task-scheduler-1.0-SNAPSHOT.jar
  
  if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
    echo Failed to download JAR file.
    echo Please check your internet connection or build manually with Maven:
    echo   mvn clean package -DskipTests
    exit /b 1
  )
  
  echo JAR file downloaded successfully!
  echo.
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
