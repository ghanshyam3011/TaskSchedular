package com.taskscheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import com.google.gson.annotations.Expose;
import java.lang.ProcessBuilder;

public class Task {
    private static final Logger logger = Logger.getLogger(Task.class.getName());
    
    @Expose
    private int id;
    
    @Expose
    private String title;
    
    @Expose
    private boolean completed;
    
    @Expose
    private LocalDateTime dueDate;
    
    @Expose
    private Set<String> tags;
    
    @Expose
    private boolean notified;
    
    @Expose
    private Duration reminderTime;
    
    @Expose
    private boolean isRecurring;
    
    @Expose
    private String recurrenceType; // "daily", "weekly", "monthly"
    
    @Expose
    private int recurrenceCount;   // How many times to repeat (optional)
    
    @Expose
    private LocalDateTime recurrenceEnd; // End date (optional)
    
    @Expose
    private int occurrencesGenerated; // To keep track
    
    @Expose
    private String cronExpression; // Store cron expression for recurring tasks
    
    @Expose
    private String command; // Added command field
    
    @Expose
    private String email; // Added email field

    public Task(int id, String title, boolean completed, LocalDateTime dueDate) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.dueDate = dueDate;
        this.tags = new HashSet<>();
        this.notified = false;
        this.reminderTime = Duration.ofHours(1); // Default reminder: 1 hour before
        this.isRecurring = false;
        this.occurrencesGenerated = 0;
        this.recurrenceCount = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        this.tags.add(tag.toLowerCase());
    }

    public void removeTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
            return;
        }
        this.tags.remove(tag.toLowerCase());
    }

    public boolean hasTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
            return false;
        }
        return this.tags.contains(tag.toLowerCase());
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isDue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !completed;
    }

    public Duration getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Duration reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        if (recurring && dueDate == null) {
            throw new IllegalArgumentException("Recurring tasks must have a due date.");
        }
        isRecurring = recurring;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        if (recurrenceType == null || 
            (!recurrenceType.equalsIgnoreCase("daily") && 
             !recurrenceType.equalsIgnoreCase("weekly") && 
             !recurrenceType.equalsIgnoreCase("monthly"))) {
            throw new IllegalArgumentException("Invalid recurrence type. Use: daily, weekly, or monthly");
        }
        this.recurrenceType = recurrenceType;
    }

    public int getRecurrenceCount() {
        return recurrenceCount;
    }

    public void setRecurrenceCount(int recurrenceCount) {
        if (recurrenceCount <= 0) {
            throw new IllegalArgumentException("Recurrence count must be a positive number.");
        }
        this.recurrenceCount = recurrenceCount;
    }

    public LocalDateTime getRecurrenceEnd() {
        return recurrenceEnd;
    }

    public void setRecurrenceEnd(LocalDateTime recurrenceEnd) {
        this.recurrenceEnd = recurrenceEnd;
    }

    public int getOccurrencesGenerated() {
        return occurrencesGenerated;
    }

    public void setOccurrencesGenerated(int occurrencesGenerated) {
        this.occurrencesGenerated = occurrencesGenerated;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Task generateNextOccurrence() {
        if (!isRecurring || recurrenceType == null) {
            return null;
        }

        // Check if we've reached the maximum number of occurrences
        if (recurrenceCount > 0) {
            int remaining = recurrenceCount - occurrencesGenerated;
            // Don't generate next occurrence if this is the last one
            if (remaining <= 1) {
                return null;
            }
        }

        // Check if we've reached the end date
        if (recurrenceEnd != null && dueDate.isAfter(recurrenceEnd)) {
            return null;
        }

        LocalDateTime nextDue;
        if (cronExpression != null) {
            nextDue = CronExpressionGenerator.getNextExecutionTime(cronExpression);
        } else {
            switch (recurrenceType.toLowerCase()) {
                case "daily":
                    nextDue = this.dueDate.plusDays(1);
                    break;
                case "weekly":
                    nextDue = this.dueDate.plusWeeks(1);
                    break;
                case "monthly":
                    nextDue = this.dueDate.plusMonths(1);
                    break;
                default:
                    return null;
            }
        }

        // Check if the next occurrence would be after the end date
        if (recurrenceEnd != null && nextDue.isAfter(recurrenceEnd)) {
            return null;
        }

        // Create the next occurrence
        Task next = new Task(0, this.title, false, nextDue); // ID will be set by TaskManager
        next.setRecurring(true);
        next.setRecurrenceType(this.recurrenceType);
        next.setOccurrencesGenerated(this.occurrencesGenerated + 1);
        next.setRecurrenceCount(this.recurrenceCount);
        next.setRecurrenceEnd(this.recurrenceEnd);
        next.setTags(new HashSet<>(this.tags));
        next.setReminderTime(this.reminderTime);
        next.setCronExpression(this.cronExpression);
        return next;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void execute() {
        try {
            logger.info("Executing task: " + title);
            
            if (command != null && !command.isEmpty()) {
                String workingDir = System.getProperty("user.dir");
                logger.info("Working directory: " + workingDir);
                
                // Create a batch file to execute the command
                File batchFile = new File(workingDir, "execute_command.bat");
                try (FileWriter writer = new FileWriter(batchFile)) {
                    writer.write("@echo off\n");
                    writer.write("cd /d \"" + workingDir + "\"\n");
                    writer.write(command + "\n");
                    writer.write("if %ERRORLEVEL% NEQ 0 (\n");
                    writer.write("  echo Command failed with error code %ERRORLEVEL%\n");
                    writer.write("  exit /b %ERRORLEVEL%\n");
                    writer.write(")\n");
                }
                
                logger.info("Created batch file at: " + batchFile.getAbsolutePath());
                
                // Execute the batch file
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("cmd.exe", "/c", batchFile.getAbsolutePath());
                processBuilder.directory(new File(workingDir));
                processBuilder.redirectErrorStream(true);
                
                logger.info("Executing batch file...");
                Process process = processBuilder.start();
                
                // Read the output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("Command output: " + line);
                    }
                }
                
                int exitCode = process.waitFor();
                logger.info("Command execution completed with exit code: " + exitCode);
                
                // Clean up the batch file
                if (batchFile.exists()) {
                    batchFile.delete();
                    logger.info("Cleaned up batch file");
                }
                
                if (exitCode == 0) {
                    logger.info("Command executed successfully for task: " + title);
                    // Verify if file was created
                    String filename = command.substring(command.indexOf(">") + 1).trim();
                    File outputFile = new File(workingDir, filename);
                    if (outputFile.exists()) {
                        logger.info("Output file created successfully at: " + outputFile.getAbsolutePath());
                        logger.info("File size: " + outputFile.length() + " bytes");
                    } else {
                        logger.warning("Output file was not created at: " + outputFile.getAbsolutePath());
                    }
                } else {
                    logger.warning("Command execution failed with exit code: " + exitCode + " for task: " + title);
                }
            }
            
            // Mark as completed after execution
            this.completed = true;
            
            // If it's a recurring task, generate the next occurrence
            if (isRecurring) {
                Task nextTask = generateNextOccurrence();
                if (nextTask != null) {
                    TaskManager.getInstance().addTask(nextTask);
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error executing task: " + title, e);
            throw new RuntimeException("Failed to execute task: " + title, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%d] %s (Completed: %s)", id, title, completed));
        
        if (dueDate != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                sb.append(" Due: ").append(dueDate.format(formatter));
            } catch (Exception e) {
                sb.append(" Due: [invalid date]");
            }
        }

        if (isRecurring) {
            sb.append(" [Recurring: ").append(recurrenceType);
            if (recurrenceCount > 0) {
                int remaining = recurrenceCount - occurrencesGenerated;
                if (remaining > 0) {
                    sb.append(", ").append(remaining).append(" remaining");
                }
            }
            sb.append("]");
        }

        if (command != null && !command.isEmpty()) {
            sb.append(" Command: ").append(command);
        }

        if (tags != null && !tags.isEmpty()) {
            sb.append(" Tags: ").append(String.join(", ", tags));
        }

        return sb.toString();
    }
}

