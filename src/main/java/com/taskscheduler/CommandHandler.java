package com.taskscheduler;

import java.util.Scanner;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.DateTimeException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.time.Duration;
import java.io.IOException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import java.io.File;
import java.util.HashSet;

public class CommandHandler {
    private final TaskManager taskManager;
    private final ReminderManager reminderManager;
    private final CommandLogger commandLogger;
    private final CommandPatternAnalyzer patternAnalyzer;
    private final LineReader reader;
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter()
    };

    public CommandHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.reminderManager = new ReminderManager(taskManager);
        this.commandLogger = new CommandLogger();
        this.patternAnalyzer = new CommandPatternAnalyzer(commandLogger);
        
        try {
            Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jansi(true)
                .build();
            
            CommandCompleter completer = new CommandCompleter(taskManager);
            
            this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .option(LineReader.Option.AUTO_FRESH_LINE, true)
                .option(LineReader.Option.INSERT_TAB, true)
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .variable(LineReader.HISTORY_FILE, new File(System.getProperty("user.home"), ".task-scheduler-history"))
                .variable(LineReader.HISTORY_SIZE, 1000)
                .variable(LineReader.HISTORY_FILE_SIZE, 10000)
                .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }
    }

    public void start() {
        System.out.println("Task Scheduler - Type 'help' for available commands");
        
        while (true) {
            try {
                // Check for command suggestions before reading input
                if (ConfigManager.isSmartSuggestionsEnabled()) {
                    String suggestion = patternAnalyzer.getCommandSuggestion(LocalDateTime.now());
                    if (suggestion != null) {
                        System.out.print("\n💡 Suggestion: " + suggestion + " [Y/n] > ");
                        String response = reader.readLine("").trim().toLowerCase();
                        if (response.isEmpty() || response.equals("y") || response.equals("yes")) {
                            handleCommands(suggestion);
                            continue;
                        }
                    }
                }

                String command = reader.readLine("> ");
                if (command == null || command.equalsIgnoreCase("exit")) {
                    System.out.println("Shutting down Task Scheduler...");
                    reminderManager.shutdown();
                    System.exit(0);
                }
                handleCommands(command);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void handleCommands(String command) {
        if (command.equalsIgnoreCase("exit")) {
            System.out.println("Shutting down Task Scheduler...");
            reminderManager.shutdown();
            return;
        }

        // Log the command before handling it
        commandLogger.logCommand(command);

        try {
            if (command.equalsIgnoreCase("help")) {
                showHelp();
            } else if (command.equalsIgnoreCase("suggestions")) {
                boolean currentState = ConfigManager.isSmartSuggestionsEnabled();
                ConfigManager.setSmartSuggestionsEnabled(!currentState);
                System.out.println("Smart suggestions are now " + (!currentState ? "enabled" : "disabled"));
            } else if (command.startsWith("email-notification ")) {
                String email = command.substring("email-notification ".length()).trim();
                handleEmailNotification(email);
            } else if (command.startsWith("add ")) {
                String taskPart = command.substring(4).trim();
                String[] parts = taskPart.split(" --");
                String[] modifiedParts = new String[parts.length];
                modifiedParts[0] = parts[0];
                for (int i = 1; i < parts.length; i++) {
                    modifiedParts[i] = "--" + parts[i];
                }
                handleAddTask(modifiedParts[0], modifiedParts);
            } else if (command.equalsIgnoreCase("list")) {
                taskManager.listTasks();
            } else if (command.equalsIgnoreCase("list upcoming")) {
                listUpcomingTasks();
            } else if (command.equalsIgnoreCase("list overdue")) {
                listOverdueTasks();
            } else if (command.startsWith("list --tag ")) {
                String tag = command.substring(11).toLowerCase();
                listTasksByTag(tag);
            } else if (command.startsWith("delete ")) {
                int taskId = Integer.parseInt(command.substring(7));
                taskManager.deleteTask(taskId);
            } else if (command.startsWith("complete ")) {
                int taskId = Integer.parseInt(command.substring(9));
                completeTask(taskId);
            } else if (command.startsWith("due ")) {
                String[] parts = command.split(" ", 3);
                if (parts.length < 3) {
                    System.out.println("Invalid command. Usage: due <id> <date>");
                    return;
                }
                int taskId = Integer.parseInt(parts[1]);
                setDueDate(taskId, parts[2]);
            } else if (command.startsWith("tag ")) {
                String[] parts = command.substring(4).split(" ", 2);
                if (parts.length < 2) {
                    System.out.println("Invalid command. Usage: tag <id> <tag1> [tag2 tag3 ...]");
                    return;
                }
                int taskId = Integer.parseInt(parts[0]);
                String[] tags = parts[1].split("\\s+");
                addTagsToTask(taskId, tags);
            } else if (command.startsWith("untag ")) {
                String[] parts = command.substring(6).split(" ", 2);
                if (parts.length < 2) {
                    System.out.println("Invalid command. Usage: untag <id> <tag1> [tag2 tag3 ...]");
                    return;
                }
                int taskId = Integer.parseInt(parts[0]);
                String[] tags = parts[1].split("\\s+");
                removeTagsFromTask(taskId, tags);
            } else if (command.startsWith("reminder ")) {
                String[] parts = command.substring(9).split(" ", 2);
                if (parts.length < 2) {
                    System.out.println("Invalid command. Usage: reminder <id> <time>");
                    return;
                }
                int taskId = Integer.parseInt(parts[0]);
                String timeStr = parts[1].toLowerCase();
                setReminderTime(taskId, timeStr);
            } else {
                showHelp();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listUpcomingTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> upcomingTasks = taskManager.getTasks().stream()
            .filter(task -> !task.isCompleted() && task.getDueDate() != null && 
                (task.getDueDate().isAfter(now) || 
                 task.getDueDate().toLocalDate().equals(now.toLocalDate())))
            .sorted((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()))
            .collect(Collectors.toList());

        if (upcomingTasks.isEmpty()) {
            System.out.println("No upcoming tasks found.");
        } else {
            System.out.println("Upcoming Tasks:");
            upcomingTasks.forEach(System.out::println);
        }
    }

    private void listOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskManager.getTasks().stream()
            .filter(task -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isBefore(now))
            .sorted((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()))
            .collect(Collectors.toList());

        if (overdueTasks.isEmpty()) {
            System.out.println("No overdue tasks found.");
        } else {
            System.out.println("Overdue Tasks:");
            overdueTasks.forEach(System.out::println);
        }
    }

    private void listTasksByTag(String tag) {
        boolean found = false;
        for (Task task : taskManager.getTasks()) {
            if (task.hasTag(tag)) {
                System.out.println(task);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No tasks found with tag: " + tag);
        }
    }

    private void addTagsToTask(int taskId, String[] tags) {
        for (Task task : taskManager.getTasks()) {
            if (task.getId() == taskId) {
                for (String tag : tags) {
                    task.addTag(tag);
                }
                taskManager.saveTasks();
                System.out.println("Tags added to task " + taskId + ": " + String.join(", ", tags));
                return;
            }
        }
        System.out.println("Task not found.");
    }

    private void removeTagsFromTask(int taskId, String[] tags) {
        for (Task task : taskManager.getTasks()) {
            if (task.getId() == taskId) {
                for (String tag : tags) {
                    task.removeTag(tag);
                }
                taskManager.saveTasks();
                System.out.println("Tags removed from task " + taskId + ": " + String.join(", ", tags));
                return;
            }
        }
        System.out.println("Task not found.");
    }

    private LocalDateTime parseTimeFromTitle(String title) {
        try {
            // First try to find a date in the title
            String[] words = title.toLowerCase().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equals("at") || words[i].equals("on")) {
                    String dateStr = words[i + 1];
                    // Try each formatter
                    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                        try {
                            return LocalDateTime.parse(dateStr, formatter);
                        } catch (DateTimeParseException e) {
                            // Continue to next formatter
                        }
                    }
                }
            }

            // If no date found, try to find time in format like "at 2pm", "at 2:30pm", etc.
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equals("at")) {
                    String timeStr = words[i + 1];
                    LocalDateTime now = LocalDateTime.now();
                    
                    // Handle 12-hour format with AM/PM
                    if (timeStr.matches("\\d{1,2}(?::\\d{2})?(?:am|pm)")) {
                        boolean isPM = timeStr.toLowerCase().endsWith("pm");
                        timeStr = timeStr.toLowerCase().replaceAll("(am|pm)", "").trim();
                        
                        String[] timeParts = timeStr.split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
                        
                        // Convert to 24-hour format
                        if (isPM && hour < 12) hour += 12;
                        if (!isPM && hour == 12) hour = 0;
                        
                        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute);
                    }
                }
            }
        } catch (Exception e) {
            // If any error occurs during parsing, return null (no time set)
            return null;
        }
        return null;
    }

    private void completeTask(int taskId) {
        taskManager.completeTask(taskId);
    }

    private void setDueDate(int taskId, String dueDateStr) {
        LocalDateTime dueDate = null;
        DateTimeParseException lastException = null;

        // Try each formatter until one works
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                dueDate = LocalDateTime.parse(dueDateStr, formatter);
                // Validate the date components
                validateDate(dueDate);
                break;
            } catch (DateTimeParseException e) {
                lastException = e;
            } catch (DateTimeException e) {
                System.out.println("Invalid date: " + e.getMessage());
                return;
            }
        }

        if (dueDate == null) {
            System.out.println("Invalid date format. Please use one of the following formats:");
            System.out.println("  yyyy-MM-dd HH:mm (e.g., 2025-05-07 17:00)");
            System.out.println("  yyyy/MM/dd HH:mm (e.g., 2025/05/07 17:00)");
            System.out.println("  dd-MM-yyyy HH:mm (e.g., 07-05-2025 17:00)");
            System.out.println("  dd/MM/yyyy HH:mm (e.g., 07/05/2025 17:00)");
            System.out.println("  yyyy-MM-dd (time will be set to 00:00)");
            return;
        }

        for (Task task : taskManager.getTasks()) {
            if (task.getId() == taskId) {
                task.setDueDate(dueDate);
                taskManager.saveTasks();
                System.out.println("Due date set for task " + taskId + ": " + 
                    dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                return;
            }
        }
        System.out.println("Task not found.");
    }

    private void validateDate(LocalDateTime date) {
        // Check if the date is in the past
        if (date.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("Cannot set due date in the past");
        }

        // Check if the year is reasonable (e.g., not too far in the future)
        int currentYear = LocalDateTime.now().getYear();
        if (date.getYear() > currentYear + 10) {
            throw new DateTimeException("Due date cannot be more than 10 years in the future");
        }

        // Validate month (1-12)
        if (date.getMonthValue() < 1 || date.getMonthValue() > 12) {
            throw new DateTimeException("Invalid month: " + date.getMonthValue());
        }

        // Validate day of month
        int maxDays = date.getMonth().length(date.toLocalDate().isLeapYear());
        if (date.getDayOfMonth() < 1 || date.getDayOfMonth() > maxDays) {
            throw new DateTimeException("Invalid day of month: " + date.getDayOfMonth() + 
                " for month " + date.getMonth());
        }

        // Validate hour (0-23)
        if (date.getHour() < 0 || date.getHour() > 23) {
            throw new DateTimeException("Invalid hour: " + date.getHour());
        }

        // Validate minute (0-59)
        if (date.getMinute() < 0 || date.getMinute() > 59) {
            throw new DateTimeException("Invalid minute: " + date.getMinute());
        }
    }

    private void setReminderTime(int taskId, String timeStr) {
        Duration reminderTime = parseReminderTime(timeStr);
        if (reminderTime == null) {
            System.out.println("Invalid reminder time format. Use format like '30m' for 30 minutes or '2h' for 2 hours.");
            return;
        }

        for (Task task : taskManager.getTasks()) {
            if (task.getId() == taskId) {
                if (task.getDueDate() == null) {
                    System.out.println("Cannot set reminder for task without due date.");
                    return;
                }
                task.setReminderTime(reminderTime);
                reminderManager.scheduleReminder(task, reminderTime);
                taskManager.saveTasks();
                System.out.println("Reminder set for task " + taskId + ": " + formatDuration(reminderTime) + " before due time");
                return;
            }
        }
        System.out.println("Task not found.");
    }

    private Duration parseReminderTime(String timeStr) {
        try {
            if (timeStr.endsWith("h")) {
                int hours = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                return Duration.ofHours(hours);
            } else if (timeStr.endsWith("m")) {
                int minutes = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                return Duration.ofMinutes(minutes);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        
        if (hours > 0) {
            return String.format("%d hour%s", hours, hours == 1 ? "" : "s");
        } else {
            return String.format("%d minute%s", minutes, minutes == 1 ? "" : "s");
        }
    }

    private void handleEmailNotification(String email) {
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format. Please provide a valid email address.");
            return;
        }

        try {
            ConfigManager.saveEmail(email);
            System.out.println("Email saved successfully. You'll now receive task reminders at: " + email);
        } catch (IOException e) {
            System.out.println("Error saving email: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidRecurrenceType(String type) {
        return type != null && (type.equalsIgnoreCase("daily") || 
                              type.equalsIgnoreCase("weekly") || 
                              type.equalsIgnoreCase("monthly"));
    }

    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        throw new DateTimeParseException("Invalid date format", dateStr, 0);
    }

    private void handleAddTask(String taskTitle, String[] parts) {
        try {
            // Extract title and time
            String[] titleParts = taskTitle.split(" at ", 2);
            if (titleParts.length != 2) {
                System.out.println("Invalid format. Use: add \"Task Title\" at HH:mm [options]");
                return;
            }

            String title = titleParts[0].trim();
            if (title.startsWith("\"") && title.endsWith("\"")) {
                title = title.substring(1, title.length() - 1);
            }

            String timeStr = titleParts[1].trim();
            LocalDateTime dueDate = parseDate(timeStr);
            validateDate(dueDate);

            Task task = new Task(0, title, false, dueDate);

            // Process additional parameters
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.startsWith("--command ")) {
                    String command = part.substring("--command ".length()).trim();
                    // Remove quotes if present
                    if (command.startsWith("\"") && command.endsWith("\"")) {
                        command = command.substring(1, command.length() - 1);
                    }
                    System.out.println("Setting command: " + command); // Debug log
                    task.setCommand(command);
                } else if (part.startsWith("--recurring ")) {
                    String type = part.substring("--recurring ".length()).trim();
                    if (isValidRecurrenceType(type)) {
                        task.setRecurring(true);
                        task.setRecurrenceType(type);
                    }
                } else if (part.startsWith("--count ")) {
                    int count = Integer.parseInt(part.substring("--count ".length()).trim());
                    task.setRecurrenceCount(count);
                } else if (part.startsWith("--end ")) {
                    String endDate = part.substring("--end ".length()).trim();
                    task.setRecurrenceEnd(parseDate(endDate));
                } else if (part.startsWith("--reminder ")) {
                    String reminderTime = part.substring("--reminder ".length()).trim();
                    task.setReminderTime(parseReminderTime(reminderTime));
                } else if (part.startsWith("--tag ")) {
                    String[] tags = part.substring("--tag ".length()).trim().split("\\s+");
                    for (String tag : tags) {
                        task.addTag(tag);
                    }
                } else if (part.startsWith("--email ")) {
                    String email = part.substring("--email ".length()).trim();
                    if (isValidEmail(email)) {
                        task.setEmail(email);
                    }
                }
            }

            // Verify command was set
            if (task.getCommand() == null || task.getCommand().trim().isEmpty()) {
                System.out.println("Warning: No command specified for task: " + title);
            }

            taskManager.addTask(task);
            System.out.println("Added task: \"" + title + "\"");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format. Use: yyyy-MM-dd HH:mm or HH:mm");
        } catch (Exception e) {
            System.out.println("Error adding task: " + e.getMessage());
        }
    }

    private void showHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Available commands:\n");
        help.append("  add <task> [due <date>] [--recurring <type> [count=N] [until=date]] --tag <tags>\n");
        help.append("    Examples:\n");
        help.append("      add \"Meeting with team\" due 2024-03-20 14:00\n");
        help.append("      add \"Daily standup\" due 2024-03-20 10:00 --recurring daily\n");
        help.append("      add \"Weekly report\" due 2024-03-20 15:00 --recurring weekly count=4\n");
        help.append("      add \"Monthly review\" due 2024-03-20 16:00 --recurring monthly until=2024-12-31\n");
        help.append("  list                            - List all tasks\n");
        help.append("  list upcoming                   - List upcoming tasks\n");
        help.append("  list overdue                    - List overdue tasks\n");
        help.append("  list --tag <tag>                - List tasks with specific tag\n");
        help.append("  delete <id>                     - Delete a task\n");
        help.append("  complete <id>                   - Mark a task as completed\n");
        help.append("  due <id> <date>                 - Set due date for a task\n");
        help.append("  tag <id> <tag1> [tag2 tag3 ...] - Add tags to a task\n");
        help.append("  untag <id> <tag1> [tag2 tag3 ...] - Remove tags from a task\n");
        help.append("  reminder <id> <time>            - Set reminder for a task (e.g., 30m or 2h)\n");
        help.append("  email-notification <email>      - Set email for task reminders\n");
        help.append("  suggestions                     - Toggle smart command suggestions\n");
        help.append("  help                            - Show this help message\n");
        help.append("  exit                            - Exit the program");
        System.out.println(help.toString());
    }
}
