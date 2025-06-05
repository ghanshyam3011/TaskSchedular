# Set classpath with all required dependencies
$classpath = "lib/*"

# Create temp_classes directory if it doesn't exist
if (-not (Test-Path "temp_classes")) {
    New-Item -ItemType Directory -Force -Path "temp_classes"
}

# Compile Java files
Write-Host "Compiling Java files..."

# First compile SilentNattyParser specifically
Write-Host "Compiling SilentNattyParser..."
& javac -d temp_classes -cp "$classpath" src/main/java/com/taskscheduler/nlp/SilentNattyParser.java

# Then compile everything else
Write-Host "Compiling remaining files..."
& javac -d temp_classes -cp "$classpath;temp_classes" src/main/java/com/taskscheduler/util/*.java src/main/java/com/taskscheduler/nlp/*.java src/main/java/com/taskscheduler/*.java

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
} else {
    Write-Host "Compilation failed!"
}