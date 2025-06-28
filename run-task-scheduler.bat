@echo off
REM Task Scheduler Launcher Batch File
echo Starting Task Scheduler Application...

REM Run the PowerShell script with bypassing execution policy
powershell -ExecutionPolicy Bypass -File "%~dp0run-task-scheduler.ps1" %*

if %ERRORLEVEL% NEQ 0 (
    echo Application exited with error code: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

exit /b 0
