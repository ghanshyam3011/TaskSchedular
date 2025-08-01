# NeuroTask Scheduler Background Service
# This script periodically checks for tasks that need to be executed
# Run this directly for testing or use NSSM to install as a Windows service

$ErrorActionPreference = "Continue"
$LogFile = Join-Path $PSScriptRoot "scheduler_log.txt"
$TaskCheckerBat = Join-Path $PSScriptRoot "run_task_checker.bat"

# Create log message with timestamp
function Write-Log {
    param (
        [string]$Message
    )
    $timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    "$timestamp - $Message" | Out-File -FilePath $LogFile -Append
}

# Log startup
Write-Log "NeuroTask background service starting..."
Write-Log "Working directory: $PSScriptRoot"

# Main loop
try {
    while ($true) {
        $currentTime = Get-Date
        Write-Log "Checking for tasks at $($currentTime.ToString("HH:mm:ss"))..."
        
        # Run the task checker
        try {
            $process = Start-Process -FilePath $TaskCheckerBat -NoNewWindow -Wait -PassThru
            if ($process.ExitCode -ne 0) {
                Write-Log "Task checker exited with code $($process.ExitCode)"
            }
        } catch {
            Write-Log "Error running task checker: $_"
        }
        
        # Wait for next check (60 seconds)
        Start-Sleep -Seconds 30
    }
} catch {
    Write-Log "Service error: $_"
    throw $_
} finally {
    Write-Log "Service stopping"
}
