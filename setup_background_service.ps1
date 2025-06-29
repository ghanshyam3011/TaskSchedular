# =============================================================================
# Task Scheduler Background Service Setup Script
# This script sets up the task scheduler as a Windows service using NSSM
# =============================================================================

# Set working directory to the script location
$appDirectory = $PSScriptRoot
Set-Location $appDirectory

# Configuration
$serviceName = "TaskScheduler"
$serviceDisplayName = "Task Scheduler Background Service"
$serviceDescription = "Automatically checks and executes scheduled tasks in the background"
$nssmPath = Join-Path $appDirectory "nssm-2.24-101-g897c7ad\win64\nssm.exe"
$powershellExe = "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"
$scriptPath = Join-Path $appDirectory "simple_scheduler.ps1"
$powershellArgs = "-ExecutionPolicy Bypass -NoProfile -File `"$scriptPath`""

# Check if NSSM exists
if (-not (Test-Path -Path $nssmPath)) {
    Write-Host "ERROR: NSSM not found at $nssmPath" -ForegroundColor Red
    Write-Host "Please extract the NSSM package to the correct location" -ForegroundColor Yellow
    exit 1
}

# Check if the service already exists
$serviceExists = Get-Service -Name $serviceName -ErrorAction SilentlyContinue

if ($serviceExists) {
    Write-Host "Service $serviceName already exists. Removing..." -ForegroundColor Yellow
    
    # Stop the service if it's running
    if ($serviceExists.Status -eq "Running") {
        Write-Host "Stopping service..." -ForegroundColor Yellow
        & $nssmPath stop $serviceName
        Start-Sleep -Seconds 2
    }
    
    # Remove the service
    Write-Host "Removing service..." -ForegroundColor Yellow
    & $nssmPath remove $serviceName confirm
    Start-Sleep -Seconds 2
}

# Install the service
Write-Host "Installing service $serviceName..." -ForegroundColor Cyan
& $nssmPath install $serviceName $powershellExe $powershellArgs

# Configure the service
Write-Host "Configuring service..." -ForegroundColor Cyan
& $nssmPath set $serviceName DisplayName $serviceDisplayName
& $nssmPath set $serviceName Description $serviceDescription
& $nssmPath set $serviceName AppDirectory $appDirectory
& $nssmPath set $serviceName AppExit Default Restart
& $nssmPath set $serviceName Start SERVICE_AUTO_START
& $nssmPath set $serviceName ObjectName LocalSystem
& $nssmPath set $serviceName AppStdout "$appDirectory\service_stdout.log"
& $nssmPath set $serviceName AppStderr "$appDirectory\service_stderr.log"

# Start the service
Write-Host "Starting service..." -ForegroundColor Green
& $nssmPath start $serviceName

# Check service status
Start-Sleep -Seconds 3
$serviceStatus = Get-Service -Name $serviceName -ErrorAction SilentlyContinue

if ($serviceStatus -and $serviceStatus.Status -eq "Running") {
    Write-Host "Service $serviceName is now running!" -ForegroundColor Green
    Write-Host "Tasks will be automatically executed in the background, even after system restart." -ForegroundColor Green
} else {
    Write-Host "Failed to start service $serviceName" -ForegroundColor Red
    Write-Host "Please check the logs for more information" -ForegroundColor Yellow
}

Write-Host "`nService Details:" -ForegroundColor Cyan
Write-Host "  Name: $serviceName" -ForegroundColor White
Write-Host "  Display Name: $serviceDisplayName" -ForegroundColor White
Write-Host "  Description: $serviceDescription" -ForegroundColor White
Write-Host "  Script: $scriptPath" -ForegroundColor White
Write-Host "`nTo manage the service:" -ForegroundColor Yellow
Write-Host "  - Start: nssm start $serviceName" -ForegroundColor White
Write-Host "  - Stop: nssm stop $serviceName" -ForegroundColor White
Write-Host "  - Remove: nssm remove $serviceName confirm" -ForegroundColor White
Write-Host "  - Edit: nssm edit $serviceName" -ForegroundColor White
