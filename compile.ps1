# Set classpath with all required dependencies
$classpath = "lib/*"

# Create temp_classes directory if it doesn't exist
if (-not (Test-Path "temp_classes")) {
    New-Item -ItemType Directory -Force -Path "temp_classes"
}

# Compile Java files
Write-Host "Compiling Java files..."
& javac -d temp_classes -cp "$classpath" src/main/java/com/taskscheduler/*.java src/main/java/com/taskscheduler/nlp/*.java src/main/java/com/taskscheduler/util/*.java src/main/java/com/taskscheduler/ui/*.java

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
} else {
    Write-Host "Compilation failed!"
}