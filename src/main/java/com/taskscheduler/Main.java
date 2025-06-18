
package com.taskscheduler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
      /**
     * Suppresses console logging for all Java logger
     */
    private static void suppressConsoleLogging() {
        try {
            // First, try to load logging properties from file
            try {
                // This will override JVM logging settings with our custom settings
                System.setProperty("java.util.logging.config.file", "natty-logging.properties");
                java.util.logging.LogManager.getLogManager().readConfiguration(); 
            } catch (Exception e) {
                System.out.println("Warning: Could not load custom logging properties file.");
            }
            
            // Set the root logger's level to suppress most logs
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.SEVERE);
            
            // Install a filter on the root logger to block all Natty logs
            rootLogger.setFilter(new com.taskscheduler.util.NattyLoggingFilter());
            
            // Also remove console handlers to ensure no logs appear
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    handler.setLevel(Level.SEVERE);
                    // Apply filter to each handler too
                    handler.setFilter(new com.taskscheduler.util.NattyLoggingFilter());
                }
            }
            
            // Specifically suppress other loggers we know about
            Logger.getLogger(QuartzScheduler.class.getName()).setLevel(Level.SEVERE);
            Logger.getLogger(TaskJob.class.getName()).setLevel(Level.SEVERE);
            Logger.getLogger(TaskManager.class.getName()).setLevel(Level.SEVERE);
            
            // Completely disable Natty parser logging - use more aggressive approach
            Logger nattyParser = Logger.getLogger("com.joestelmach.natty.Parser");
            nattyParser.setLevel(Level.OFF);
            nattyParser.setUseParentHandlers(false);
            for (Handler h : nattyParser.getHandlers()) {
                nattyParser.removeHandler(h);
            }
            
            // For even more thorough suppression, add a do-nothing handler
            nattyParser.addHandler(new ConsoleHandler() {
                @Override
                public void publish(java.util.logging.LogRecord record) {
                    // Do nothing - suppress all output
                }
                
                @Override
                public void flush() {}
                
                @Override
                public void close() {}
            });
            
            // Suppress all related loggers too - more aggressively
            silenceLogger("com.joestelmach");
            silenceLogger("com.joestelmach.natty");
            silenceLogger("net.objectlab");
            silenceLogger("org.antlr");
            silenceLogger("org.quartz");
        } catch (Exception e) {
            System.out.println("Warning: Error occurred while configuring logging: " + e);
        }
    }
    
    /**
     * Helper method to completely silence a logger by name
     */
    private static void silenceLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
    }    public static void main(String[] args) {
        // Set SLF4J system properties to suppress Quartz logging BEFORE any logging initialization
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.quartz", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.quartz.core", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.quartz.impl", "error");
        System.setProperty("org.slf4j.simpleLogger.log.org.quartz.simpl", "error");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        
        try {
            // Check if running in background mode first
            boolean isBackgroundMode = false;
            if (args.length > 0) {
                String firstArg = args[0].trim();
                
                // Remove any quotes that might be included
                if (firstArg.startsWith("\"") && firstArg.endsWith("\"")) {
                    firstArg = firstArg.substring(1, firstArg.length() - 1);
                }
                
                // Check for background mode parameter
                if (firstArg.equals("--check-tasks") || 
                    firstArg.equalsIgnoreCase("--check-tasks") ||
                    firstArg.contains("--check-tasks")) {
                    
                    isBackgroundMode = true;
                    System.out.println("Starting in background mode to check for due tasks");
                    runInBackgroundMode();
                    return; // Exit immediately after background execution
                }
            }
            
            // Set default logging level based on execution mode
            boolean isDebugMode = System.getProperty("debug") != null;
            
            // Suppress console logs in interactive mode unless debug is enabled
            if (!isDebugMode && !isBackgroundMode) {
                suppressConsoleLogging();
            }
            
            // Normal interactive mode
            runInInteractiveMode();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting application", e);
            System.exit(1);
        }
    }
    
    private static void runInInteractiveMode() {
        // Initialize TaskManager singleton
        TaskManager taskManager = TaskManager.getInstance();
        
        // Initialize Quartz scheduler
        QuartzScheduler scheduler = QuartzScheduler.getInstance();
        
        // Schedule existing tasks
        for (Task task : taskManager.getTasks()) {
            if (task.getDueDate() != null && !task.isCompleted()) {
                scheduler.scheduleTask(task);
            }
        }
        
        // Start command handler
        CommandHandler commandHandler = new CommandHandler(taskManager);
        
        // Add shutdown hook to gracefully stop the scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down scheduler...");
            scheduler.shutdown();
        }));
        
        commandHandler.start();
    }
      private static void runInBackgroundMode() {
        System.out.println("Checking for due tasks...");
        BackgroundTaskRunner runner = new BackgroundTaskRunner();
        int executedTasks = runner.checkAndExecuteTasks();
        System.out.println("Background check completed. Executed " + executedTasks + " tasks");
        
        // Exit the application
        System.exit(0);
    }
} 