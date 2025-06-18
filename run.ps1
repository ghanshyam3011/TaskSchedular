# Set the classpath with all required dependencies and compiled classes
$classpath = "temp_classes;lib/*;."

# Run the application
Write-Host "Starting Task Scheduler application..."
java "-cp" "$classpath" "-Djava.util.logging.config.file=src/main/resources/logging.properties" "-Dnatty.logger.level=SEVERE" "-Dcom.joestelmach.natty.level=SEVERE" "-Dorg.antlr.level=SEVERE" "-Dnet.objectlab.kit.datecalc.level=SEVERE" "com.taskscheduler.Main"