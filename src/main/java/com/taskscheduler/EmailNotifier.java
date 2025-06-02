package com.taskscheduler;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.time.Duration;

public class EmailNotifier {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = System.getenv("EMAIL_USERNAME");
    private static final String SMTP_PASSWORD = System.getenv("EMAIL_PASSWORD");

    public static void sendTaskReminder(String toEmail, Task task, Duration reminderTime) {
        if (SMTP_USERNAME == null || SMTP_PASSWORD == null) {
            System.out.println("Email configuration not found. Please set EMAIL_USERNAME and EMAIL_PASSWORD environment variables.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

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
            message.setSubject("Task Reminder: " + task.getTitle());

            String emailBody = String.format(
                "Task Reminder\n\n" +
                "Task: %s\n" +
                "Due: %s\n" +
                "Reminder: %d minutes before due time\n\n" +
                "This is an automated reminder from your Task Scheduler.",
                task.getTitle(),
                task.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                reminderTime.toMinutes()
            );

            message.setText(emailBody);

            Transport.send(message);
            System.out.println("Reminder email sent to " + toEmail);
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
} 