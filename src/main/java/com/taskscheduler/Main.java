
package com.taskscheduler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    // stops console logging for java loggers
    private static void suppressConsoleLogging() {
        try {
            // load logging properties from file
            try {
                System.setProperty("java.util.logging.config.file", "natty-logging.properties");
                java.util.logging.LogManager.getLogManager().readConfiguration(); 
            } catch (Exception e) {
                System.out.println("Warning: Could not load custom logging properties file.");
            }
            
            // set root logger level
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.SEVERE);
            
            // remove console handlers
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    handler.setLevel(Level.SEVERE);
                }
            }
            
            // suppress other loggers
            Logger.getLogger(QuartzScheduler.class.getName()).setLevel(Level.SEVERE);
            Logger.getLogger(TaskJob.class.getName()).setLevel(Level.SEVERE);
            Logger.getLogger(TaskManager.class.getName()).setLevel(Level.SEVERE);
            
            // disable natty parser logging
            Logger nattyParser = Logger.getLogger("com.joestelmach.natty.Parser");
            nattyParser.setLevel(Level.OFF);
            nattyParser.setUseParentHandlers(false);
            for (Handler h : nattyParser.getHandlers()) {
                nattyParser.removeHandler(h);
            }
            
            nattyParser.addHandler(new ConsoleHandler() {
                @Override
                public void publish(java.util.logging.LogRecord record) {
                    // suppress output
                }
                
                @Override
                public void flush() {}
                
                @Override
                public void close() {}
            });
            
            // silence related loggers
            suppressLogger("com.joestelmach");
            suppressLogger("net.objectlab");
            suppressLogger("org.antlr");
            suppressLogger("org.quartz");
        } catch (Exception e) {
            System.out.println("Warning: Error occurred while configuring logging: " + e);
        }
    }
    
    // silences a specific logger by name
    private static void suppressLogger(String name) {
        Logger loggerInstance = Logger.getLogger(name);
        loggerInstance.setLevel(Level.OFF);
        loggerInstance.setUseParentHandlers(false);
        for (Handler h : loggerInstance.getHandlers()) {
            loggerInstance.removeHandler(h);
        }
    }
    
    public static void main(String[] args) {
        try {
            // check for background mode
            if (args.length > 0) {
                String firstArg = args[0].trim();
                
                // remove quotes if present
                if (firstArg.startsWith("\"") && firstArg.endsWith("\"")) {
                    firstArg = firstArg.substring(1, firstArg.length() - 1);
                }
                
                if (firstArg.equals("--check-tasks") || 
                    firstArg.equalsIgnoreCase("--check-tasks") ||
                    firstArg.contains("--check-tasks")) {
                    
                    System.out.println("Starting in background mode to check for due tasks");
                    runInBackgroundMode();
                    return;
                }
            }
            
            boolean isDebugMode = System.getProperty("debug") != null;
            
            if (!isDebugMode) {
                suppressConsoleLogging();
            }
            
            runInInteractiveMode();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting application", e);
            System.exit(1);
        }
    }
    
    private static void runInInteractiveMode() {
        TaskManager taskManager = TaskManager.getInstance();
        QuartzScheduler scheduler = QuartzScheduler.getInstance();
        
        // schedule existing tasks
        for (Task task : taskManager.getTasks()) {
            if (task.getDueDate() != null && !task.isCompleted()) {
                scheduler.scheduleTask(task);
            }
        }
        
        org.fusesource.jansi.AnsiConsole.systemInstall();
        CommandHandler commandHandler = new CommandHandler(taskManager);
        
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down scheduler...");
            scheduler.shutdown();
            org.fusesource.jansi.AnsiConsole.systemUninstall();
        }));
        
        // show welcome screen
        try {
            com.taskscheduler.ui.InteractiveUI.showWelcome();
        } catch (Exception e) {
            com.taskscheduler.ui.UIManager.displayWelcome();
        }
        
        commandHandler.start();
    }
    
    private static void runInBackgroundMode() {
        System.out.println("Checking for due tasks...");
        BackgroundTaskRunner runner = new BackgroundTaskRunner();
        int executedTasks = runner.checkAndExecuteTasks();
        System.out.println("Background check completed. Executed " + executedTasks + " tasks");
        
        System.exit(0);
    }
} 