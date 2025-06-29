@echo off
REM ============================================================
REM Task Scheduler Background Task Checker
REM This script checks and executes due tasks in the background
REM ============================================================

REM Set the application directory to the location of this script
set APP_DIR=%~dp0
cd /d "%APP_DIR%"

REM Configure log file
set LOG_FILE=task_checker_log.txt
set OUTPUT_LOG=task_checker_output.log

REM Output timestamp
echo. >> %LOG_FILE%
echo ===== Task Check Started: %date% %time% ===== >> %LOG_FILE%

REM Run the Java application in task checking mode
java -cp "temp_classes;lib\*;." com.taskscheduler.Main --check-tasks >> %OUTPUT_LOG% 2>&1

REM Output completion
echo Task check completed with exit code: %ERRORLEVEL% >> %LOG_FILE%
echo ===== Task Check Completed: %date% %time% ===== >> %LOG_FILE%
echo. >> %LOG_FILE%

REM Return successful exit code
exit /b 0
