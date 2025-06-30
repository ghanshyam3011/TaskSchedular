@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Building and running TaskScheduler in Docker...
echo.

REM Stop and remove any existing container
docker stop taskscheduler 2>nul
docker rm taskscheduler 2>nul

REM Build the image
echo Building Docker image...
docker build -t taskscheduler .

REM Run the container in interactive mode
echo Starting application in Docker container...
echo.
docker run -it --name taskscheduler --rm taskscheduler java -jar app.jar

echo.
echo Container stopped.
pause
