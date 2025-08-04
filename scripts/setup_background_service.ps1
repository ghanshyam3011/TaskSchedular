# Sets up task scheduler as a Windows service - requires admin rights
$ErrorActionPreference = "Stop"
$ServiceName = "NeuroTaskScheduler"
$ScriptPath = Join-Path $PSScriptRoot "simple_scheduler.ps1"
$NssmPath = Join-Path $PSScriptRoot "..\nssm-2.24-101-g897c7ad\win64\nssm.exe"
$Description = "NeuroTask Scheduler Background Service"

# Check required files
if (-not (Test-Path $ScriptPath)) {
    Write-Host "ERROR: Scheduler script not found at: $ScriptPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $NssmPath)) {
    Write-Host "ERROR: NSSM not found at: $NssmPath" -ForegroundColor Red
    Write-Host "Please extract the NSSM package to the correct folder location" -ForegroundColor Yellow
    exit 1
}

# Verify admin rights
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "ERROR: Please run this script as Administrator" -ForegroundColor Red
    exit 1
}

# Remove existing service if found
$existingService = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if ($existingService) {
    Write-Host "Service already exists. Removing..." -ForegroundColor Yellow
    & $NssmPath remove $ServiceName confirm
}

# Create service
Write-Host "Installing scheduler service..." -ForegroundColor Cyan

# Configure and install service
& $NssmPath install $ServiceName powershell.exe "-ExecutionPolicy Bypass -NoProfile -File `"$ScriptPath`""
& $NssmPath set $ServiceName Description $Description
& $NssmPath set $ServiceName DisplayName "NeuroTask Scheduler Service"
& $NssmPath set $ServiceName Start SERVICE_AUTO_START
& $NssmPath set $ServiceName AppStdout (Join-Path $PSScriptRoot "..\service_stdout.log")
& $NssmPath set $ServiceName AppStderr (Join-Path $PSScriptRoot "..\service_stderr.log")

# Start service
Start-Service -Name $ServiceName

# Report status
$status = (Get-Service -Name $ServiceName).Status
Write-Host "Service status: $status" -ForegroundColor Green
Write-Host ""
Write-Host "Service installed and started successfully!" -ForegroundColor Green
Write-Host "Tasks will run automatically in the background." -ForegroundColor Green
Write-Host ""
Write-Host "Commands:" -ForegroundColor Yellow
Write-Host "  Get-Service -Name $ServiceName" -ForegroundColor Yellow
Write-Host "  Stop-Service -Name $ServiceName" -ForegroundColor Yellow
Write-Host "  Start-Service -Name $ServiceName" -ForegroundColor Yellow
