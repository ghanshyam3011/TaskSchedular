@echo off
REM Set console to UTF-8 mode for emoji support
chcp 65001 > nul
echo Starting Task Scheduler application...

REM Set working directory to the script location
cd /d "%~dp0"

REM Make sure we have a directory for task outputs
if not exist "task_outputs" mkdir task_outputs

REM Set environment variable for foreground mode
set BACKGROUND_MODE=false

REM Run the Java application with UTF-8 encoding
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target\task-scheduler-1.0-SNAPSHOT.jar %*

pause
exit /b 0
