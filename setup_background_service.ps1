# NeuroTask Background Service Setup
# This script installs the task scheduler as a Windows service
# Run as Administrator

$ErrorActionPreference = "Stop"
$ServiceName = "NeuroTaskScheduler"
$ScriptPath = Join-Path $PSScriptRoot "simple_scheduler.ps1"
$NssmPath = Join-Path $PSScriptRoot "nssm-2.24-101-g897c7ad\win64\nssm.exe"
$Description = "NeuroTask Scheduler Background Service"

# Verify paths exist
if (-not (Test-Path $ScriptPath)) {
    Write-Host "ERROR: Scheduler script not found at: $ScriptPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $NssmPath)) {
    Write-Host "ERROR: NSSM not found at: $NssmPath" -ForegroundColor Red
    Write-Host "Please extract the NSSM package to the correct folder location" -ForegroundColor Yellow
    exit 1
}

# Check if running as admin
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "ERROR: Please run this script as Administrator" -ForegroundColor Red
    exit 1
}

# Check if service already exists
$existingService = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if ($existingService) {
    Write-Host "Service already exists. Removing..." -ForegroundColor Yellow
    & $NssmPath remove $ServiceName confirm
}

# Create the service
Write-Host "Installing NeuroTask Scheduler as Windows service..." -ForegroundColor Cyan
Write-Host "Using NSSM from: $NssmPath" -ForegroundColor Cyan
Write-Host "Service will run: $ScriptPath" -ForegroundColor Cyan

# Install service
& $NssmPath install $ServiceName powershell.exe "-ExecutionPolicy Bypass -NoProfile -File `"$ScriptPath`""
& $NssmPath set $ServiceName Description $Description
& $NssmPath set $ServiceName DisplayName "NeuroTask Scheduler Service"
& $NssmPath set $ServiceName Start SERVICE_AUTO_START
& $NssmPath set $ServiceName AppStdout (Join-Path $PSScriptRoot "service_stdout.log")
& $NssmPath set $ServiceName AppStderr (Join-Path $PSScriptRoot "service_stderr.log")

# Start the service
Write-Host "Starting service..." -ForegroundColor Green
Start-Service -Name $ServiceName

# Check service status
$status = (Get-Service -Name $ServiceName).Status
Write-Host "Service status: $status" -ForegroundColor Green
Write-Host ""
Write-Host "NeuroTask Scheduler service has been installed and started successfully!" -ForegroundColor Green
Write-Host "Your scheduled tasks will now run automatically in the background." -ForegroundColor Green
Write-Host ""
Write-Host "To check service status: Get-Service -Name $ServiceName" -ForegroundColor Yellow
Write-Host "To stop service: Stop-Service -Name $ServiceName" -ForegroundColor Yellow
Write-Host "To start service: Start-Service -Name $ServiceName" -ForegroundColor Yellow
