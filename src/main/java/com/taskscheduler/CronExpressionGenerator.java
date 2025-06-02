package com.taskscheduler;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CronExpressionGenerator {
    
    /**
     * Generates a cron expression for a daily recurring task at a specific time
     * @param time The time of day for the task
     * @return A cron expression in the format "0 minute hour * * *"
     */
    public static String generateDailyCronExpression(LocalTime time) {
        return String.format("0 %d %d * * *", time.getMinute(), time.getHour());
    }

    /**
     * Generates a cron expression for a weekly recurring task at a specific time and day
     * @param time The time of day for the task
     * @param dayOfWeek The day of week (1-7, where 1 is Monday)
     * @return A cron expression in the format "0 minute hour * * dayOfWeek"
     */
    public static String generateWeeklyCronExpression(LocalTime time, int dayOfWeek) {
        return String.format("0 %d %d * * %d", time.getMinute(), time.getHour(), dayOfWeek);
    }

    /**
     * Generates a cron expression for a monthly recurring task at a specific time and day
     * @param time The time of day for the task
     * @param dayOfMonth The day of month (1-31)
     * @return A cron expression in the format "0 minute hour dayOfMonth * *"
     */
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