package com.taskscheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderManager {
    private final TaskManager taskManager;
    private final ScheduledExecutorService scheduler;

    public ReminderManager(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            taskManager.checkRecurringTasks();
        }, 0, 1, TimeUnit.HOURS);
    }

    public void scheduleReminder(Task task, Duration reminderTime) {
        if (task.getDueDate() == null) {
            return;
        }

        LocalDateTime reminderDateTime = task.getDueDate().minus(reminderTime);
        long delay = Duration.between(LocalDateTime.now(), reminderDateTime).toMinutes();
        
        if (delay > 0) {
            scheduler.schedule(() -> {
                com.taskscheduler.ui.UIManager.displayInfo("⏰ Reminder: Task \"" + task.getTitle() + "\" is due in " + 
                    reminderTime.toMinutes() + " minutes!");

                String userEmail = ConfigManager.getEmail();
                if (userEmail != null && !userEmail.trim().isEmpty()) {
                    try {
                        EmailNotifier.sendTaskReminder(userEmail, task, reminderTime);
                    } catch (Exception e) {
                        com.taskscheduler.ui.UIManager.displayError("Error sending reminder email: " + e.getMessage());
                    }
                }
            }, delay, TimeUnit.MINUTES);
        }
    }

    public void shutdown() {
        try {
            scheduler.shutdown();
            
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 