# Task Scheduler Background Service - Setup & Testing Guide

This guide provides step-by-step instructions for setting up and testing the Task Scheduler background service, which allows tasks to be executed automatically even when the main application is closed or the system is restarted.

## Background Service Feature

The Task Scheduler background service enables:

- **Fully automated task execution** - Tasks are executed at their scheduled time, even if the main app is closed
- **System reboot resilience** - Tasks continue to be monitored after your computer restarts
- **Hands-free operation** - No need to manually launch anything to execute scheduled tasks
- **Email notifications** - Receive notifications when tasks are executed

## Files Overview

The background service consists of three key files:

1. `run_task_checker.bat` - Runs the Java application in task-checking mode
2. `simple_scheduler.ps1` - PowerShell script that runs in a loop, executing the task checker every minute
3. `setup_background_service.ps1` - Sets up the PowerShell script as a Windows service

## Prerequisites

- Windows operating system
- Administrator privileges (for service installation)
- NSSM (Non-Sucking Service Manager) extracted to `nssm-2.24-101-g897c7ad` folder

## Setup Instructions

### Option 1: Run in Foreground (Testing)

1. Open PowerShell and navigate to your Task Scheduler directory
2. Run the PowerShell script directly:
   ```
   .\simple_scheduler.ps1
   ```
3. Keep the PowerShell window open - the script will check for tasks every 60 seconds
4. You can view activity in `scheduler_log.txt`

### Option 2: Install as Windows Service (Recommended)

1. Ensure NSSM is extracted to the correct folder
2. Open PowerShell as Administrator
3. Navigate to your Task Scheduler directory
4. Run:
   ```
   .\setup_background_service.ps1
   ```
5. This will install and start a Windows service that runs in the background
6. The service will start automatically when Windows boots up

## Testing the Background Service

### Method 1: Create a Future Task with Command

1. Start the main Task Scheduler application using `run-task-scheduler.bat`
2. Add a task that will run in the future (5 minutes from now)
3. Include a command like:
   ```
   echo Test successful at %date% %time% > test_background_success.txt
   ```
4. Close the Task Scheduler application
5. Wait for the scheduled time to pass
6. Check if `test_background_success.txt` has been created

### Method 2: Use Sleep and Check Logs

1. Start the main Task Scheduler application
2. Add a task due in 2 minutes with a command
3. Close the application
4. Wait for 3 minutes
5. Check the following log files:
   - `scheduler_log.txt` - Shows the schedule checks
   - `task_checker_log.txt` - Shows individual check activities
   - `task_checker_output.log` - Shows task execution outputs
   - Look for the task execution in the `task_outputs` directory

### Method 3: Email Notification Test

1. Start the main Task Scheduler application
2. Configure email notifications with:
   ```
   email-notification youremail@example.com
   ```
3. Add a task due in 2 minutes with:
   ```
   add "Email test task" due 2025-06-29 12:45 --command "echo Email test successful" --notify-email
   ```
4. Close the application
5. Wait for the email notification to arrive

## Checking Service Status

1. Open PowerShell as Administrator
2. Run:
   ```
   Get-Service -Name TaskScheduler
   ```
3. Check if the service is running

## Troubleshooting

- If tasks aren't executing, check the log files for errors
- Ensure the Java application is compiled properly (with `--rebuild` flag)
- Verify the service is running (using the Get-Service command)
- Check Windows Event Viewer for service-related errors

## Additional Notes

- The background service checks for tasks every 30 seconds
- Tasks are executed precisely when they're due (within a 1-minute precision window)
- Log files are created in the main application directory
- Task outputs are saved to the `task_outputs` directory
