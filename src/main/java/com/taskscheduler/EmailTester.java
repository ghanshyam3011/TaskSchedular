package com.taskscheduler;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for testing email notifications
 */
public class EmailTester {
    
    public static void main(String[] args) {
        System.out.println("=== EMAIL NOTIFICATION TEST ===");
        
        // Test email configuration loading
        System.out.println("Testing email configuration...");
        String userEmail = ConfigManager.getEmail();
        System.out.println("User email from config: " + (userEmail != null ? userEmail : "NULL"));
        
        if (userEmail == null || userEmail.trim().isEmpty()) {
            System.out.println("ERROR: No user email configured in config.json");
            System.out.println("Please set the email in config.json or use the 'email <your-email>' command in the main app");
            return;
        }
        
        // Create a test task
        System.out.println("Creating test task...");
        Task testTask = new Task(999, "Email Test Task", false, LocalDateTime.now());
        testTask.setCommand("echo This is a test email notification");
        
        // Test email sending
        System.out.println("Attempting to send test email notification...");
        try {
            EmailNotifier.sendTaskReminder(userEmail, testTask, Duration.ofMinutes(0));
            System.out.println("Test email sending attempt completed.");
            System.out.println("Check your email inbox (and spam folder) for the test notification.");
        } catch (Exception e) {
            System.out.println("Error during email test: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== EMAIL NOTIFICATION TEST COMPLETE ===");
    }
    
    /**
     * Test method that can be called from other classes
     */
    public static void testEmailNotification() {
        String userEmail = ConfigManager.getEmail();
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            Task testTask = new Task(998, "Background Email Test", false, LocalDateTime.now());
            testTask.setCommand("echo Background email test");
            EmailNotifier.sendTaskReminder(userEmail, testTask, Duration.ofMinutes(0));
        } else {
            System.out.println("No email configured for testing.");
        }
    }
}
