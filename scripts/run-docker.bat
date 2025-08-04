@echo off
chcp 65001 > nul
echo Running NeuroTask in Docker...

cd /d "%~dp0.."

if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
  echo JAR file not found. Creating dummy JAR...
  
  if not exist "target" mkdir target
  
  echo Manifest-Version: 1.0 > MANIFEST.MF
  echo Main-Class: DemoMain >> MANIFEST.MF
  echo. >> MANIFEST.MF
  
  echo public class DemoMain { > DemoMain.java
  echo     public static void main^(String[] args^) { >> DemoMain.java
  echo         System.out.println^(\"NeuroTask Scheduler - Demo\"^); >> DemoMain.java
  echo         System.out.println^(\"Build with Maven for full app.\"^); >> DemoMain.java
  echo         System.out.println^(\"Press Ctrl+C to exit.\"^); >> DemoMain.java
  echo         while^(true^) { try { Thread.sleep^(60000^); } catch^(Exception e^) {} } >> DemoMain.java
  echo     } >> DemoMain.java
  echo } >> DemoMain.java
  
  javac DemoMain.java
  if errorlevel 1 (
    echo dummy jar > target\task-scheduler-1.0-SNAPSHOT.jar
  ) else (
    jar cmf MANIFEST.MF target\task-scheduler-1.0-SNAPSHOT.jar DemoMain.class
    del DemoMain.class
  )
  
  del MANIFEST.MF
  del DemoMain.java
  
  echo Dummy JAR created.
)

docker stop neurotask 2>nul
docker rm neurotask 2>nul

echo Building image...
docker build -t neurotask .

echo Starting container...
docker run -it --name neurotask --rm neurotask

echo Container stopped.
pause
