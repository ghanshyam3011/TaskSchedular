package com.taskscheduler;

import org.quartz.*;
import java.util.logging.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class TaskJob implements Job {
    private static final Logger logger = Logger.getLogger(TaskJob.class.getName());
    private static TaskManager taskManager = TaskManager.getInstance();
    private static final String OUTPUT_DIR = "task_outputs";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            int taskId = context.getJobDetail().getJobDataMap().getInt("taskId");
            Task task = taskManager.getTaskById(taskId);
            
            if (task != null) {
                logger.info("Executing scheduled task: " + task.getTitle());
                
                // Create output directory if it doesn't exist
                File outputDir = new File(OUTPUT_DIR);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                // Generate output filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String outputFile = String.format("%s/task_%d_%s.txt", OUTPUT_DIR, taskId, timestamp);
                
                // Execute the command
                ProcessBuilder builder = new ProcessBuilder();
                String command = task.getCommand();
                
                if (command == null || command.trim().isEmpty()) {
                    logger.warning("No command specified for task: " + task.getTitle());
                    return;
                }

                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    builder.command("cmd.exe", "/c", command);
                } else {
                    builder.command("sh", "-c", command);
                }
                
                // Set working directory to current directory
                builder.directory(new File(System.getProperty("user.dir")));
                
                // Redirect output to file
                builder.redirectOutput(new File(outputFile));
                builder.redirectErrorStream(true);
                
                logger.info("Executing command: " + command);
                Process process = builder.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    logger.info("Task executed successfully. Output saved to: " + outputFile);
                } else {
                    logger.warning("Task execution completed with exit code: " + exitCode);
                }
                
                // Mark task as completed
                task.setCompleted(true);
                taskManager.updateTask(task);
                
            } else {
                logger.warning("Task not found for ID: " + taskId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing scheduled task", e);
            throw new JobExecutionException(e);
        }
    }
} 