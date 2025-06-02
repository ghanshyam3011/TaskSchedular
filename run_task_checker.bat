@echo off
REM Background Task Checker for TaskSchedular
REM This script runs every few minutes to check for and execute due tasks

REM Set environment variables for email notifications
set EMAIL_USERNAME=ghanshyamthacker07@gmail.com
set EMAIL_PASSWORD=ghjw qxgt jofk jklx

REM Change to the application directory
cd /d "d:\tascSaved\TaskSchedular-main"

REM Log the start of execution
echo Starting task checker at %date% %time% > task_checker_log.txt

REM Print classpath and Java command for debugging
echo Classpath: target/task-scheduler-1.0-SNAPSHOT.jar >> task_checker_log.txt
echo Command: java -cp target/task-scheduler-1.0-SNAPSHOT.jar com.taskscheduler.Main --check-tasks >> task_checker_log.txt

REM Check if JAR file exists
if not exist "target\task-scheduler-1.0-SNAPSHOT.jar" (
    echo ERROR: JAR file not found! >> task_checker_log.txt
    exit /b 1
)

REM Run the application in check-tasks mode with output logging
echo Running Java with --check-tasks parameter >> task_checker_log.txt

REM Use redirection to capture both stdout and stderr
echo Running directly in background mode using the newly compiled classes
REM Use the newly compiled classes in temp_classes directory
java -cp "temp_classes;target/task-scheduler-1.0-SNAPSHOT.jar" com.taskscheduler.Main --check-tasks > task_checker_output.log 2>&1
set EXIT_CODE=%ERRORLEVEL%

REM Log the execution time and result for debugging
echo Task check executed at %date% %time% with exit code %EXIT_CODE% >> task_checker_log.txt
if %EXIT_CODE% NEQ 0 (
    echo ERROR: Java execution failed with code %EXIT_CODE% >> task_checker_log.txt
)

REM Display any errors from the Java execution
type task_checker_output.log >> task_checker_log.txt
