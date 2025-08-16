package com.taskscheduler;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CronExpressionGenerator {
    
    // Creates daily cron expression (format: "0 minute hour * * *")
    public static String generateDailyCronExpression(LocalTime time) {
        return String.format("0 %d %d * * *", time.getMinute(), time.getHour());
    }

    // Creates weekly cron expression (format: "0 minute hour * * dayOfWeek")
    public static String generateWeeklyCronExpression(LocalTime time, int dayOfWeek) {
        return String.format("0 %d %d * * %d", time.getMinute(), time.getHour(), dayOfWeek);
    }

    // Creates monthly cron expression (format: "0 minute hour dayOfMonth * *")
    public static String generateMonthlyCronExpression(LocalTime time, int dayOfMonth) {
        return String.format("0 %d %d %d * *", time.getMinute(), time.getHour(), dayOfMonth);
    }

    /**
     * Parses a cron expression to get the next execution time
     * @param cronExpression The cron expression to parse
     * @return The next execution time based on the cron expression
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression) {
        String[] parts = cronExpression.split("\\s+");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid cron expression format");
        }

        LocalDateTime now = LocalDateTime.now();
        int minute = Integer.parseInt(parts[1]);
        int hour = Integer.parseInt(parts[2]);
        LocalDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        if (next.isBefore(now)) {
            next = next.plusDays(1);
        }

        return next;
    }

    /**
     * Converts a LocalDateTime to a cron expression for a daily task
     * @param dateTime The date and time to convert
     * @return A cron expression for a daily task at the specified time
     */
    public static String dateTimeToDailyCron(LocalDateTime dateTime) {
        return generateDailyCronExpression(dateTime.toLocalTime());
    }
} 