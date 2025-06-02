Write-Host "Starting Task Scheduler..."

$appDirectory = "d:\tascSaved\TaskSchedular-main"
Set-Location $appDirectory

$logFile = Join-Path $appDirectory "scheduler_log.txt"
"[$(Get-Date)] Task Scheduler started" | Out-File -FilePath $logFile -Append

function Log-Message {
    param([string]$message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] $message"
    $logEntry | Out-File -FilePath $logFile -Append
    Write-Host $logEntry
}

$batchFile = Join-Path $appDirectory "run_task_checker.bat"
if (-not (Test-Path $batchFile)) {
    Log-Message "ERROR: Batch file not found: $batchFile"
    exit 1
}

$now = Get-Date
$waitTime = 60 - $now.Second
Log-Message "Waiting $waitTime seconds to start at next minute..."
Start-Sleep -Seconds $waitTime

Log-Message "Scheduler active - checking tasks every 60 seconds"

while ($true) {
    try {
        $startTime = Get-Date
        Log-Message "Checking for due tasks..."
        
        Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$batchFile`"" -Wait -WindowStyle Hidden
        
        $endTime = Get-Date
        $elapsedSeconds = ($endTime - $startTime).TotalSeconds
        $waitSeconds = 60 - $elapsedSeconds
        
        if ($waitSeconds -lt 5) { $waitSeconds = 5 }
        if ($waitSeconds -gt 60) { $waitSeconds = 60 }
        
        Log-Message "Task completed. Waiting $([math]::Round($waitSeconds, 1)) seconds..."
        Start-Sleep -Seconds $waitSeconds
        
    } catch {
        Log-Message "ERROR: $($_.Exception.Message)"
        Start-Sleep -Seconds 30
    }
}