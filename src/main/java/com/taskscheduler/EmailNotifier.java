package com.taskscheduler;

import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmailNotifier {
    private static String SMTP_HOST = "smtp.gmail.com";
    private static String SMTP_PORT = "587";
    private static String SMTP_USERNAME;
    private static String SMTP_PASSWORD;
    
    static {
        // Try to load from email-config.json first
        loadEmailConfig();
        
        // Fall back to environment variables if config file loading failed
        if (SMTP_USERNAME == null || SMTP_USERNAME.isEmpty()) {
            SMTP_USERNAME = System.getenv("EMAIL_USERNAME");
        }
        if (SMTP_PASSWORD == null || SMTP_PASSWORD.isEmpty()) {
            SMTP_PASSWORD = System.getenv("EMAIL_PASSWORD");
        }
    }
    
    /**
     * Loads email configuration from email-config.json file
     */
    private static void loadEmailConfig() {
        File configFile = new File("email-config.json");
        if (!configFile.exists()) {
            // Email config file not found - will try environment variables
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject smtp = config.getAsJsonObject("smtp");
            
            SMTP_HOST = smtp.get("host").getAsString();
            SMTP_PORT = smtp.get("port").getAsString();
            SMTP_USERNAME = smtp.get("username").getAsString();
            SMTP_PASSWORD = smtp.get("password").getAsString();
            
            // Email configuration loaded successfully (silent)
        } catch (Exception e) {
            // Email configuration error - will try environment variables
        }
    }

    public static void sendTaskReminder(String toEmail, Task task, Duration reminderTime) {
        if (SMTP_USERNAME == null || SMTP_PASSWORD == null) {
            com.taskscheduler.ui.UIManager.displayError("Email configuration not found. Please set EMAIL_USERNAME and EMAIL_PASSWORD environment variables.");
            return;
        }

        if (toEmail == null || toEmail.trim().isEmpty()) {
            com.taskscheduler.ui.UIManager.displayError("Recipient email address is null or empty.");
            return;
        }

        com.taskscheduler.ui.UIManager.displayInfo("Sending email reminder to " + toEmail + "...");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.debug", "false"); // Disable debug output

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            
            String subject = "Task Reminder: " + task.getTitle();
            message.setSubject(subject);

            String emailBody = String.format(
                "Task Reminder\n\n" +
                "Task: %s\n" +
                "Due: %s\n" +
                "Reminder: %d minutes before due time\n\n" +
                "This is an automated reminder from your Task Scheduler.\n" +
                "Sent at: %s",
                task.getTitle(),
                task.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                reminderTime.toMinutes(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            message.setText(emailBody);
            Transport.send(message);
            com.taskscheduler.ui.UIManager.displaySuccess("Email reminder sent successfully to " + toEmail);
        } catch (MessagingException e) {
            com.taskscheduler.ui.UIManager.displayError("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            com.taskscheduler.ui.UIManager.displayError("Unexpected error sending email: " + e.getMessage());
        }
    }
} 