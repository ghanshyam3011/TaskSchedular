@echo off
REM TaskSchedular - Start Background Scheduler
REM This script starts the PowerShell scheduler in the background

echo ========================================================
echo    TaskSchedular - Starting Background Task Scheduler
echo ========================================================

REM Start PowerShell script minimized and in the background
powershell -WindowStyle Minimized -ExecutionPolicy Bypass -File "%~dp0simple_scheduler.ps1"

echo Background scheduler started successfully!
echo You can close this window - tasks will continue to run automatically.
echo.
echo View scheduler_log.txt for execution details.
echo.
pause
