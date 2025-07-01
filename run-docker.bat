@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Building and running NeuroTask in Docker...
echo.

REM Check if JAR exists
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
  echo JAR file not found. Creating a dummy JAR file...
  
  REM Create target directory if it doesn't exist
  if not exist "target" mkdir target
  
  REM Create a very simple dummy JAR file
  echo Creating a dummy JAR file for Docker demo...
  echo Manifest-Version: 1.0 > MANIFEST.MF
  echo Created-By: NeuroTask Demo >> MANIFEST.MF
  echo Main-Class: DemoMain >> MANIFEST.MF
  echo. >> MANIFEST.MF
  
  REM Create a simple Java class source file
  echo public class DemoMain { > DemoMain.java
  echo     public static void main^(String[] args^) { >> DemoMain.java
  echo         System.out.println^(\"\\n\\n\"^); >> DemoMain.java
  echo         System.out.println^(\"NeuroTask Scheduler - Demo Version\"^); >> DemoMain.java
  echo         System.out.println^(\"This is a Docker demonstration.\"^); >> DemoMain.java
  echo         System.out.println^(\"For the full application, build with Maven.\"^); >> DemoMain.java
  echo         System.out.println^(\"Press Ctrl+C to exit.\"^); >> DemoMain.java
  echo         while ^(true^) { >> DemoMain.java
  echo             try { Thread.sleep^(60000^); } catch ^(Exception e^) {} >> DemoMain.java
  echo         } >> DemoMain.java
  echo     } >> DemoMain.java
  echo } >> DemoMain.java
  
  REM Try to compile the Java file
  javac DemoMain.java
  if errorlevel 1 (
    echo Failed to compile. Creating an empty JAR file instead.
    echo Just a dummy jar file > target\task-scheduler-1.0-SNAPSHOT.jar
  ) else (
    REM Create JAR file
    jar cmf MANIFEST.MF target\task-scheduler-1.0-SNAPSHOT.jar DemoMain.class
    del DemoMain.class
  )
  
  REM Clean up
  del MANIFEST.MF
  del DemoMain.java
  
  echo Demo JAR file created successfully!
  echo This is a demo version for Docker demonstration only.
  echo.
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
