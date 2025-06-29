package com.taskscheduler;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles execution of tasks in background mode
 * This class is used when the application is launched with --check-tasks parameter
 */
public class BackgroundTaskRunner {
    private static final Logger LOGGER = Logger.getLogger(BackgroundTaskRunner.class.getName());
    private final TaskManager taskManager;
    private final QuartzScheduler scheduler;
    private static final String OUTPUT_DIR = "task_outputs";
    
    public BackgroundTaskRunner() {
        this.taskManager = TaskManager.getInstance();
        this.scheduler = QuartzScheduler.getInstance(); // Fixed: Use getInstance() instead of constructor
    }
    
    /**
     * Checks for due tasks and executes them
     * @return The number of tasks executed
     */    public int checkAndExecuteTasks() {
        List<Task> tasks = taskManager.getTasks();
        int executedTasks = 0;
        LocalDateTime now = LocalDateTime.now();
        LOGGER.info("BackgroundTaskRunner: Checking for due tasks at " + now);
        LOGGER.info("BackgroundTaskRunner: Found " + tasks.size() + " total tasks in the system");        for (Task task : tasks) {
            if (!task.isCompleted() && task.getDueDate() != null) {
                // Check if task is due - we want to be more precise with execution timing
                // Only execute tasks that are:
                // 1. Due right now (or within 1 minute in the past)
                // 2. Overdue but not executed yet (within reasonable bounds)
                LocalDateTime dueTime = task.getDueDate();
                LocalDateTime oneMinuteAgo = now.minusMinutes(1);
                LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
                
                LOGGER.info("BackgroundTaskRunner: Checking task " + task.getId() + ": " + task.getTitle());
                LOGGER.info("BackgroundTaskRunner: Task due time: " + dueTime);
                LOGGER.info("BackgroundTaskRunner: Current time: " + now);
                LOGGER.info("BackgroundTaskRunner: Time window: Current time (± 1 minute) or overdue");
                LOGGER.info("BackgroundTaskRunner: Task completed: " + task.isCompleted());
                LOGGER.info("BackgroundTaskRunner: Task has email: " + (task.getEmail() != null ? task.getEmail() : "NO EMAIL"));
                
                // Only execute if:
                // - Task is due right now (within 1 minute precision)
                // - OR task is overdue (within last 30 minutes) but not executed yet
                boolean isDueNow = (dueTime.isAfter(oneMinuteAgo) && dueTime.isBefore(now.plusMinutes(1))) || dueTime.equals(now);
                boolean isRecentlyOverdue = dueTime.isAfter(thirtyMinutesAgo) && dueTime.isBefore(now);
                
                if (isDueNow || isRecentlyOverdue) {
                    LOGGER.info("BackgroundTaskRunner: *** EXECUTING TASK: " + task.getTitle() + " ***");
                    
                    // Execute the task directly without using TaskJob.execute()
                    executeTask(task);
                    executedTasks++;
                      // Handle recurring tasks
                    if (task.isRecurring()) {
                        // Fixed: Use scheduleTask instead of scheduleRecurringTask
                        scheduler.scheduleTask(task);
                    } else {
                        // Mark non-recurring tasks as completed
                        task.setCompleted(true);
                        taskManager.saveTasks();
                    }
                } else {
                    LOGGER.info("BackgroundTaskRunner: Task " + task.getId() + " not in due window - skipping");
                }
            } else {
                if (task.isCompleted()) {
                    LOGGER.info("BackgroundTaskRunner: Task " + task.getId() + " is completed - skipping");
                }
                if (task.getDueDate() == null) {
                    LOGGER.info("BackgroundTaskRunner: Task " + task.getId() + " has no due date - skipping");
                }
            }
        }
        
        return executedTasks;
    }
    
    /**
     * Executes a task directly instead of using the Quartz job executor
     * @param task The task to execute
     */    private void executeTask(Task task) {
        try {
            // Create output directory if it doesn't exist
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Generate output filename with timestamp
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String outputFile = String.format("%s/task_%d_%s.txt", OUTPUT_DIR, task.getId(), timestamp);
              // Execute the command
            ProcessBuilder builder = new ProcessBuilder();
            String command = task.getCommand();
              if (command == null || command.trim().isEmpty()) {
                LOGGER.warning("No command specified for task: " + task.getTitle());
                return;
            }            // Fix path formatting issues - replace missing backslashes in Windows paths
            // This helps with commands like "echo text > d:pathtofile" where backslashes get lost
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Look for Windows drive letters without proper path separators
                command = command.replaceAll("([A-Za-z]:)([^\\\\/ ])", "$1\\\\$2");
                  // Fix paths where backslashes are entirely missing (e.g., d:tascSavedTaskSchedular-mainfile.txt)
                command = command.replaceAll("([A-Za-z]:\\\\?)(tascSaved)(TaskSchedular-main)", "$1tascSaved\\\\TaskSchedular-main");
                
                // Add backslashes between key path components based on known patterns
                command = command.replaceAll("([A-Za-z]:\\\\)tascSaved\\\\TaskSchedular-main([^\\\\])", "$1tascSaved\\\\TaskSchedular-main\\\\$2");
                
                LOGGER.info("Executing Windows command with fixed paths: " + command);
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            
            // Set working directory to current directory
            builder.directory(new File(System.getProperty("user.dir")));
            
            // Redirect output to file
            builder.redirectOutput(new File(outputFile));
            builder.redirectErrorStream(true);
            
            LOGGER.info("Executing command: " + command);
            Process process = builder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                LOGGER.info("Task executed successfully. Output saved to: " + outputFile);
            } else {
                LOGGER.warning("Task execution completed with exit code: " + exitCode);
            }            // Handle email notification if configured
            String taskEmail = task.getEmail();
            String userEmail = ConfigManager.getEmail();
            String emailToUse = taskEmail != null ? taskEmail : userEmail;
            
            if (emailToUse != null && !emailToUse.trim().isEmpty()) {
                try {
                    System.out.println("Sending email notification for task: " + task.getTitle());
                    EmailNotifier.sendTaskReminder(emailToUse, task, Duration.ofMinutes(0));
                    System.out.println("Email notification sent successfully to: " + emailToUse);
                } catch (Exception e) {
                    System.out.println("Failed to send email notification: " + e.getMessage());
                }
            } else {
                System.out.println("No email configured for notifications.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing task: " + task.getTitle(), e);
        }
    }
}
