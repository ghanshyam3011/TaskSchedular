@echo off
chcp 65001 > nul
echo Starting Task Scheduler...

cd /d "%~dp0.."

if not exist "task_outputs" mkdir task_outputs

set BACKGROUND_MODE=false

java -Dfile.encoding=UTF-8 -jar target\task-scheduler-1.0-SNAPSHOT.jar %*

pause
exit /b 0
