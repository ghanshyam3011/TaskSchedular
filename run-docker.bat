@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Building and running NeuroTask in Docker...
echo.

REM Check if JAR exists
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
  echo JAR file not found. Creating a minimal JAR file...
  
  REM Create target directory if it doesn't exist
  if not exist "target" mkdir target
  
  REM Create the demo JAR directly using the pre-built one
  echo Downloading the demo JAR file...
  
  REM Use PowerShell to create a minimal JAR
  powershell -Command "Invoke-WebRequest -Uri 'https://gist.githubusercontent.com/ghanshyam3011/f2d51e79c1c8b5e615dd9382a0002b5c/raw/c1d93769139db603582e722f7b27fdfae7b7d705/neurotask-demo.jar' -OutFile 'target\task-scheduler-1.0-SNAPSHOT.jar'"
  
  if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
    echo Failed to create the demo JAR.
    echo Please try again with internet access or build manually with Maven:
    echo   mvn clean package -DskipTests
    exit /b 1
  )
  
  echo Demo JAR file created successfully!
  echo This is a demo version with limited functionality.
  echo.
  
  REM Find Java compiler
  where javac >nul 2>&1
  if %ERRORLEVEL% NEQ 0 (
    echo Java compiler not found. Please install JDK or use a pre-built JAR file.
    exit /b 1
  )
  
  REM Compile the Java file
  echo Compiling Java source...
  javac temp_classes\com\taskscheduler\ui\UIManager.java
  
  REM Create a manifest file for the JAR
  echo Main-Class: com.taskscheduler.ui.UIManager > temp_classes\MANIFEST.MF
  
  REM Create the JAR file
  echo Creating JAR file...
  cd temp_classes
  jar cfm ..\target\task-scheduler-1.0-SNAPSHOT.jar MANIFEST.MF com\taskscheduler\ui\UIManager.class
  cd ..
  
  if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
    echo Failed to create JAR file.
    echo Please build manually with Maven:
    echo   mvn clean package -DskipTests
    exit /b 1
  )
  
  echo Demo JAR file created successfully!
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
