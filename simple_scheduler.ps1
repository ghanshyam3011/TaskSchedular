# =============================================================================
# Task Scheduler Background Service PowerShell Script
# This script runs the task checker continuously in the background
# =============================================================================

# Set error action preference to silently continue to avoid crashes on errors
$ErrorActionPreference = "SilentlyContinue"

# Set working directory to the script location
$appDirectory = $PSScriptRoot
Set-Location $appDirectory

# Configure log file
$logFile = "scheduler_log.txt"

# Function to write to log file with timestamp
function Write-Log {
    param (
        [string]$message
    )
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp - $message" | Out-File -FilePath $logFile -Append
}

# Log script start
Write-Log "Background scheduler service started"

# Define the batch file to call
$batchFile = Join-Path $appDirectory "run_task_checker.bat"

# Check if the batch file exists
if (-not (Test-Path -Path $batchFile)) {
    Write-Log "ERROR: Batch file not found at $batchFile"
    exit 1
}

try {
    Write-Log "Starting background task checker loop"
    
    # Infinite loop to run the task checker continuously
    while ($true) {
        # Run the batch file
        $processStart = Get-Date
        Write-Log "Running task checker..."
        
        # Call the batch file using Start-Process to avoid window popups
        $process = Start-Process -FilePath $batchFile -NoNewWindow -PassThru
        
        # Wait for the process to complete
        $process.WaitForExit()
        
        # Log the result
        $processEnd = Get-Date
        $duration = ($processEnd - $processStart).TotalSeconds
        $exitCode = $process.ExitCode
        Write-Log "Task checker completed with exit code: $exitCode (Duration: $duration seconds)"

        # Sleep for 30 seconds before next check
        Write-Log "Waiting 30 seconds before next check..."
        Start-Sleep -Seconds 30
    }
} catch {
    # Log any exceptions
    Write-Log "ERROR: $($_.Exception.Message)"
    exit 1
}
