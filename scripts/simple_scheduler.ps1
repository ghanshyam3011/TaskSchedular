# Background task checker service
# Runs scheduled tasks when application is not open

$ErrorActionPreference = "Continue"
$LogFile = Join-Path $PSScriptRoot "..\scheduler_log.txt"
$TaskCheckerBat = Join-Path $PSScriptRoot "..\run_task_checker.bat"

# Log with timestamp
function Write-Log {
    param ([string]$Message)
    $timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    "$timestamp - $Message" | Out-File -FilePath $LogFile -Append
}

# Init
Write-Log "Background service starting"
Write-Log "Directory: $PSScriptRoot"

# Task checking loop
try {
    while ($true) {
        $currentTime = Get-Date
        Write-Log "Checking tasks at $($currentTime.ToString("HH:mm:ss"))"
        
        # Run checker
        try {
            $process = Start-Process -FilePath $TaskCheckerBat -NoNewWindow -Wait -PassThru
            if ($process.ExitCode -ne 0) {
                Write-Log "Task checker exit code: $($process.ExitCode)"
            }
        } catch {
            Write-Log "Error: $_"
        }
        
        # Wait 30 seconds before next check
        Start-Sleep -Seconds 30
    }
} catch {
    Write-Log "Service error: $_"
    throw $_
} finally {
    Write-Log "Service stopping"
}
