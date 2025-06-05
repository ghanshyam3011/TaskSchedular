# Set the classpath with all required dependencies and compiled classes
$classpath = "temp_classes;lib/*;."

# Run the application
Write-Host "Starting Task Scheduler application..."
java "-cp" "$classpath" `
"-Djava.util.logging.config.file=src/main/resources/logging.properties" `
"-Dcom.joestelmach.natty.Parser.level=OFF" `
"-Dcom.joestelmach.natty.level=OFF" `
"-Dcom.joestelmach.level=OFF" `
"-Dorg.antlr.level=OFF" `
"-Dnet.objectlab.kit.level=OFF" `
"-Dnet.objectlab.level=OFF" `
"-Dnatty.logger.level=OFF" `
"-Djava.util.logging.ConsoleHandler.level=INFO" `
"-Dorg.slf4j.simpleLogger.defaultLogLevel=ERROR" `
"-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog" `
"com.taskscheduler.Main"