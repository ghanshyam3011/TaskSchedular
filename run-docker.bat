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
  
  REM Create a minimal executable JAR file with a Main class
  echo Creating minimal JAR file...
  
  REM Create a temporary directory for our class files
  if not exist "temp_classes\com\taskscheduler\ui" mkdir temp_classes\com\taskscheduler\ui
  
  REM Write a simple Java source file
  echo public class UIManager { > temp_classes\com\taskscheduler\ui\UIManager.java
  echo     public static void main(String[] args) { >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("\n\n\n"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("╔════════════════════════════════════════════════════════════════╗"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("║                                                                ║"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("║  🚀 NEUROTASK SCHEDULER                                      ║"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("║                                                                ║"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("║  DEMO VERSION - CREATED FOR DOCKER DEMONSTRATION            ║"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("║                                                                ║"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("╚════════════════════════════════════════════════════════════════╝"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("\n\n"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("This is a minimal demo version of NeuroTask Scheduler."); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("For the full version, please build from source using Maven."); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("\n"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("To exit, press Ctrl+C or type 'exit'"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         System.out.println("\n"); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         while (true) { >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo             System.out.print("NeuroTask > "); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo             try { >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                 String input = new java.util.Scanner(System.in).nextLine(); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                 if ("exit".equalsIgnoreCase(input)) { >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                     System.out.println("Exiting..."); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                     break; >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                 } >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                 System.out.println("Command not implemented in demo version."); >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo             } catch (Exception e) { >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo                 break; >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo             } >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo         } >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo     } >> temp_classes\com\taskscheduler\ui\UIManager.java
  echo } >> temp_classes\com\taskscheduler\ui\UIManager.java
  
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
