# PowerShell script to create a Windows Task Scheduler task
# This will run the task checker every 5 minutes

$taskName = "TaskSchedular Background Checker"
$taskDescription = "Checks for due tasks in TaskSchedular application"
$batchFilePath = "D:\tascSaved\TaskSchedular-main\run_task_checker.bat"
$workingDir = "D:\tascSaved\TaskSchedular-main"

# Create a new task action that runs the batch file
$action = New-ScheduledTaskAction -Execute $batchFilePath -WorkingDirectory $workingDir

# Create a trigger that runs every 1 minute for more responsive task execution
# Using 10 years as duration instead of TimeSpan::MaxValue which is too large for Task Scheduler
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Minutes 1) -RepetitionDuration (New-TimeSpan -Days 3650)

# Set principal to run with highest privileges
$principal = New-ScheduledTaskPrincipal -UserId "$env:USERDOMAIN\$env:USERNAME" -LogonType S4U -RunLevel Highest

# Create the task settings
$settings = New-ScheduledTaskSettingsSet -DontStopOnIdleEnd -StartWhenAvailable -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries

# Register the scheduled task
try {
    Register-ScheduledTask -TaskName $taskName -Description $taskDescription -Action $action -Trigger $trigger -Principal $principal -Settings $settings -ErrorAction Stop
    Write-Host "Scheduled task created successfully! TaskSchedular will now run in background every 1 minute."
} catch {
    Write-Host "Error creating scheduled task: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "To fix this issue:" -ForegroundColor Yellow
    Write-Host "1. Close this window" -ForegroundColor Yellow
    Write-Host "2. Right-click on PowerShell and select 'Run as administrator'" -ForegroundColor Yellow
    Write-Host "3. Navigate to the script location: cd 'd:\tascSaved\TaskSchedular-main'" -ForegroundColor Yellow
    Write-Host "4. Run the script again: .\setup_background_service.ps1" -ForegroundColor Yellow
    exit 1
}
