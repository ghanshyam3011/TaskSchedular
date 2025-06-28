# =============================================================================
# Task Scheduler All-in-One PowerShell Script
# This script handles everything needed to run the Task Scheduler application
# =============================================================================

# Display colorful header
Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "             Task Scheduler Application Launcher              " -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan

# Set working directory to the script location
$appDirectory = $PSScriptRoot
Set-Location $appDirectory

# Function for colorful output
function Write-ColorOutput {
    param([string]$message, [string]$color = "White")
    Write-Host $message -ForegroundColor $color
}

# Create necessary directories
$directories = @("lib", "temp_classes", "task_outputs")
foreach ($dir in $directories) {
    if (-not (Test-Path -Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
        Write-ColorOutput "Created directory: $dir" "Yellow"
    }
}

Write-ColorOutput "Checking dependencies..." "Cyan"

# First, remove any existing incompatible JAR files that might cause conflicts
$filesToRemove = @(
    "lib/jansi-2.4.0.jar",
    "lib/jline-3.20.0.jar",
    "lib/jline-reader-3.20.0.jar",
    "lib/jline-terminal-3.20.0.jar",
    "lib/jline-terminal-jansi-3.20.0.jar"
)

foreach ($file in $filesToRemove) {
    if (Test-Path -Path $file) {
        Write-ColorOutput "Removing incompatible dependency: $file" "Yellow"
        Remove-Item -Path $file -Force
    }
}

# Define all required dependencies with URLs and output paths - using compatible versions
$dependencies = @(
    @{url="https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"; output="lib/gson-2.10.1.jar"},
    @{url="https://repo1.maven.org/maven2/org/quartz-scheduler/quartz/2.3.2/quartz-2.3.2.jar"; output="lib/quartz-2.3.2.jar"},
    @{url="https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar"; output="lib/slf4j-api-1.7.30.jar"},
    @{url="https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar"; output="lib/slf4j-simple-1.7.30.jar"},
    @{url="https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar"; output="lib/javax.mail-1.6.2.jar"},
    @{url="https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar"; output="lib/activation-1.1.1.jar"},
    @{url="https://repo1.maven.org/maven2/org/antlr/antlr-runtime/3.5.2/antlr-runtime-3.5.2.jar"; output="lib/antlr-runtime-3.5.2.jar"},
    @{url="https://repo1.maven.org/maven2/com/joestelmach/natty/0.13/natty-0.13.jar"; output="lib/natty-0.13.jar"},
    @{url="https://repo1.maven.org/maven2/org/apache/opennlp/opennlp-tools/2.2.0/opennlp-tools-2.2.0.jar"; output="lib/opennlp-tools-2.2.0.jar"},
    @{url="https://repo1.maven.org/maven2/com/vdurmont/emoji-java/5.1.1/emoji-java-5.1.1.jar"; output="lib/emoji-java-5.1.1.jar"},
    @{url="https://repo1.maven.org/maven2/org/json/json/20170516/json-20170516.jar"; output="lib/json-20170516.jar"},
    @{url="https://repo1.maven.org/maven2/org/beryx/text-io/3.4.1/text-io-3.4.1.jar"; output="lib/text-io-3.4.1.jar"},
    
    # Using the compatible versions for JLine and Jansi
    @{url="https://repo1.maven.org/maven2/jline/jline/2.14.6/jline-2.14.6.jar"; output="lib/jline-2.14.6.jar"},
    @{url="https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/1.18/jansi-1.18.jar"; output="lib/jansi-1.18.jar"}
)

# Download any missing dependencies
$dependenciesDownloaded = $false
foreach ($dep in $dependencies) {
    if (-not (Test-Path -Path $dep.output)) {
        $dependenciesDownloaded = $true
        Write-ColorOutput "Downloading $($dep.output)..." "Yellow"
        try {
            Invoke-WebRequest -Uri $dep.url -OutFile $dep.output
        }
        catch {
            Write-ColorOutput "Failed to download $($dep.output): $_" "Red"
            Write-ColorOutput "Press any key to exit..." "Yellow"
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            exit 1
        }
    }
}

if ($dependenciesDownloaded) {
    Write-ColorOutput "Dependencies downloaded successfully." "Green"
} else {
    Write-ColorOutput "All dependencies are available." "Green"
}

# Check if we need to build
$needsBuild = $false

# Check if the main class exists
if (-not (Test-Path -Path "temp_classes\com\taskscheduler\Main.class")) {
    $needsBuild = $true
    Write-ColorOutput "No compiled classes found. Need to build." "Yellow"
}

# Check if --rebuild flag was passed
if ($args -contains "--rebuild") {
    $needsBuild = $true
    Write-ColorOutput "Rebuild flag detected. Will recompile all sources." "Yellow"
}

# Check if any source files are newer than compiled files
if (-not $needsBuild) {
    $sourceFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
    if ($sourceFiles -and $sourceFiles.Count -gt 0) {
        $latestSource = ($sourceFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime
        
        $compiledFiles = Get-ChildItem -Path "temp_classes" -Recurse -File -ErrorAction SilentlyContinue
        if ($compiledFiles -and $compiledFiles.Count -gt 0) {
            $latestCompiled = ($compiledFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime
            
            if ($latestSource -gt $latestCompiled) {
                $needsBuild = $true
                Write-ColorOutput "Source files have been modified. Need to rebuild." "Yellow"
            }
        } else {
            $needsBuild = $true
            Write-ColorOutput "No compiled files found. Need to build." "Yellow"
        }
    }
}

if ($needsBuild) {
    Write-ColorOutput "Compiling Java files..." "Cyan"
    
    # Set classpath with all required dependencies
    $classpath = "lib/*"
    
    # Compile Java files
    javac -d temp_classes -cp "$classpath" src/main/java/com/taskscheduler/*.java src/main/java/com/taskscheduler/nlp/*.java src/main/java/com/taskscheduler/util/*.java src/main/java/com/taskscheduler/ui/*.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "Compilation failed!" "Red"
        Write-ColorOutput "Press any key to exit..." "Yellow"
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        exit 1
    }
    
    Write-ColorOutput "Compilation successful!" "Green"
} else {
    Write-ColorOutput "No build needed. Using existing compiled files." "Green"
}

# Set UTF-8 encoding for better Unicode support
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

# Set the classpath with all required dependencies and compiled classes
$classpath = "temp_classes;lib\*;."

# Run the application
Write-ColorOutput "Starting Task Scheduler application..." "Green"
Write-ColorOutput "Running: java -cp `"$classpath`" -Djava.util.logging.config.file=`"natty-logging.properties`" -Dnatty.logger.level=SEVERE -Dcom.joestelmach.natty.level=SEVERE -Dorg.antlr.level=SEVERE -Dnet.objectlab.kit.datecalc.level=SEVERE com.taskscheduler.Main" "Cyan"

java "-cp" "$classpath" "-Djava.util.logging.config.file=natty-logging.properties" "-Dnatty.logger.level=SEVERE" "-Dcom.joestelmach.natty.level=SEVERE" "-Dorg.antlr.level=SEVERE" "-Dnet.objectlab.kit.datecalc.level=SEVERE" "com.taskscheduler.Main" $args

if ($LASTEXITCODE -ne 0) {
    Write-ColorOutput "Application exited with error code: $LASTEXITCODE" "Red"
    Write-ColorOutput "Press any key to exit..." "Yellow"
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit $LASTEXITCODE
}
