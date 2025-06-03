
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
        // Set the root logger's level to suppress most logs
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.SEVERE);
        
        // Also remove console handlers to ensure no logs appear
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.SEVERE);
            }
        }
        
        // Specifically suppress other loggers we know about
        Logger.getLogger(QuartzScheduler.class.getName()).setLevel(Level.SEVERE);
        Logger.getLogger(TaskJob.class.getName()).setLevel(Level.SEVERE);
        Logger.getLogger(TaskManager.class.getName()).setLevel(Level.SEVERE);
    }

    public static void main(String[] args) {
        try {
            // Set default logging level based on execution mode
            boolean isDebugMode = System.getProperty("debug") != null;
            boolean isBackgroundMode = args.length > 0 && args[0].contains("--check-tasks");
            
            // Suppress console logs in interactive mode unless debug is enabled
            if (!isDebugMode && !isBackgroundMode) {
                suppressConsoleLogging();
            }
            
            // Log arguments for debugging
            logger.info("***DEBUG*** Application started with " + args.length + " arguments");
            if (args.length > 0) {
                logger.info("***DEBUG*** First argument: [" + args[0] + "]");
                // Add detailed logging for the first argument's characters
                logger.info("***DEBUG*** First argument length: " + args[0].length());
                for (int i = 0; i < args[0].length(); i++) {
                    logger.info("***DEBUG*** Character at " + i + ": " + (int)args[0].charAt(i));
                }
            } else {
                logger.info("***DEBUG*** No arguments provided");
            }
            
            // Check if running in background mode
            if (args.length > 0) {
                // Enhanced parameter handling with multiple checks
                String firstArg = args[0];
                
                // Remove any quotes that might be included
                if (firstArg.startsWith("\"") && firstArg.endsWith("\"")) {
                    firstArg = firstArg.substring(1, firstArg.length() - 1);
                    logger.info("***DEBUG*** Stripped quotes from argument: now [" + firstArg + "]");
                }
                
                // Trim and normalize the argument
                firstArg = firstArg.trim();
                
                // Try multiple comparison methods to handle common issues
                if (firstArg.equals("--check-tasks") || 
                    firstArg.equalsIgnoreCase("--check-tasks") ||
                    firstArg.contains("--check-tasks")) {
                    
                    logger.info("***DEBUG*** Running in background mode because --check-tasks parameter was detected");
                    runInBackgroundMode();
                    System.exit(0); // Explicitly exit to avoid interactive mode
                    return;
                } else {
                    // Detailed logging for debugging
                    logger.info("***DEBUG*** Argument provided but not matched: [" + firstArg + "] vs [--check-tasks]");
                    logger.info("***DEBUG*** String length comparison: " + firstArg.length() + " vs 12");
                    logger.info("***DEBUG*** String equals ignoreCase: " + firstArg.equalsIgnoreCase("--check-tasks"));
                    logger.info("***DEBUG*** String contains: " + firstArg.contains("--check-tasks"));
                    
                    // Print character codes to check for hidden/special characters
                    logger.info("***DEBUG*** Character codes for provided argument:");
                    for (int i = 0; i < firstArg.length(); i++) {
                        char c = firstArg.charAt(i);
                        logger.info("***DEBUG*** Char at " + i + ": '" + c + "' (ASCII: " + (int)c + ")");
                    }
                    
                    logger.info("***DEBUG*** Character codes for expected argument:");
                    String expected = "--check-tasks";
                    for (int i = 0; i < expected.length(); i++) {
                        char c = expected.charAt(i);
                        logger.info("***DEBUG*** Char at " + i + ": '" + c + "' (ASCII: " + (int)c + ")");
                    }
                }
            }
            
            // Normal interactive mode
            logger.info("***DEBUG*** Running in interactive mode");
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
        logger.info("Starting in background mode to check for due tasks");
        BackgroundTaskRunner runner = new BackgroundTaskRunner();
        int executedTasks = runner.checkAndExecuteTasks();
        logger.info("Background check completed. Executed " + executedTasks + " tasks");
        
        // Explicitly exit the application to prevent it from continuing to interactive mode
        logger.info("Background task execution complete - exiting application");
        System.exit(0);
    }
} 