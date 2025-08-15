package com.taskscheduler;

import java.io.File;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.taskscheduler.nlp.NLPProcessor;
import com.taskscheduler.nlp.NLPProcessor.ProcessedCommand;

public class CommandHandler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());
    private final TaskManager taskManager;
    private final ReminderManager reminderManager;
    private final CommandLogger commandLogger;
    private final CommandPatternAnalyzer patternAnalyzer;
    private final LineReader reader;
    private final NLPProcessor nlpProcessor;    
    
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        new DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().getYear())
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, LocalDateTime.now().getMonthValue())
            .parseDefaulting(ChronoField.DAY_OF_MONTH, LocalDateTime.now().getDayOfMonth())
            .toFormatter(),
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
        this.nlpProcessor = new NLPProcessor();
        
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
    }    public void start() {
        com.taskscheduler.ui.UIManager.displayWelcome();
        
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

                com.taskscheduler.ui.UIManager.displayCommandPrompt();
                String command = reader.readLine("");
                if (command == null || command.equalsIgnoreCase("exit")) {
                    com.taskscheduler.ui.UIManager.displayInfo("Shutting down Task Scheduler...");
                    reminderManager.shutdown();
                    System.exit(0);
                }
                handleCommands(command);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Runtime error: " + e.getMessage());
            }
            
            // Ensure proper spacing after each command execution
            com.taskscheduler.ui.UIManager.ensureCommandSpacing();
        }
    }

    public void handleCommands(String command) {
        if (command.equalsIgnoreCase("exit")) {
            System.out.println("Shutting down Task Scheduler...");
            reminderManager.shutdown();
            return;
        }
        
        // Add new menu command to launch the interactive UI
        if (command.trim().equalsIgnoreCase("menu")) {
            handleInteractiveMenu();
            return;
        }

        // Log the command before handling it
        commandLogger.logCommand(command);        try {
            // Check if this is already a structured command
            if (isStructuredCommand(command)) {
                // Skip NLP processing and execute directly
                executeCommand(command);
                return;
            }
            
            // Try to process as natural language first
            ProcessedCommand processedCommand = nlpProcessor.processInput(command);            if (processedCommand != null) {
                // If successful, show what we understood with beautiful UI
                String formattedCmd = processedCommand.getFormattedCommand();
                com.taskscheduler.ui.UIManager.displayInfo("✓ I understood: \"" + formattedCmd + "\"");
                
                // Execute the processed command without reprocessing
                handleFormattedCommand(formattedCmd);
                return;
            }
            
            // If NLP processing didn't work, handle as regular command
            executeCommand(command);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Handles a formatted command without attempting to process it as natural language again
     */
    private void handleFormattedCommand(String formattedCommand) {
        try {
            executeCommand(formattedCommand);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error executing command: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error executing command: " + e.getMessage());
        }
    }
    
    /**
     * Executes a command after it has been processed
     */    private void executeCommand(String command) {
        try {            if (command.equalsIgnoreCase("help")) {
                com.taskscheduler.ui.UIManager.displayHelp();
            } else if (command.equalsIgnoreCase("clear") || command.equalsIgnoreCase("refresh") || command.equalsIgnoreCase("cls") || 
                    command.toLowerCase().contains("clear screen") || command.toLowerCase().contains("refresh screen")) {
                clearScreen();
                System.out.println("Screen refreshed!");
            } else if (command.equalsIgnoreCase("suggestions")) {
                boolean currentState = ConfigManager.isSmartSuggestionsEnabled();
                ConfigManager.setSmartSuggestionsEnabled(!currentState);
                System.out.println("Smart suggestions are now " + (!currentState ? "enabled" : "disabled"));} else if (command.startsWith("email-notification ")) {
                // This is a system command to set the default email address
                String email = command.substring("email-notification ".length()).trim();
                System.out.println("Setting up email notification with: " + email);
                handleEmailNotification(email);            } else if (command.equalsIgnoreCase("test-email")) {
                // Test email notification functionality
                System.out.println("Testing email notification system...");
                EmailTester.testEmailNotification();
            } else if (command.equalsIgnoreCase("debug")) {
                // Display debug information including Unicode support
                System.out.println("\n" + com.taskscheduler.ui.Colors.CYAN + "Debug Information:" + com.taskscheduler.ui.Colors.RESET);
                System.out.println(com.taskscheduler.ui.Icons.getUnicodeInfo());
            } else if (command.startsWith("add ")) {
                String taskPart = command.substring(4).trim();
                String[] parts = taskPart.split(" --");
                String[] modifiedParts = new String[parts.length];
                modifiedParts[0] = parts[0];
                for (int i = 1; i < parts.length; i++) {
                    modifiedParts[i] = "--" + parts[i];
                }
                handleAddTask(modifiedParts[0], modifiedParts);            } else if (command.equalsIgnoreCase("list")) {
                com.taskscheduler.ui.UIManager.displayTasksTable(taskManager.getTasks());
            } else if (command.equalsIgnoreCase("list upcoming")) {
                listUpcomingTasks();
            } else if (command.equalsIgnoreCase("list overdue")) {
                listOverdueTasks();
            } else if (command.startsWith("list --tag ")) {
                String tag = command.substring(11).toLowerCase();
                listTasksByTag(tag);            } else if (command.startsWith("view ")) {
                int taskId = Integer.parseInt(command.substring(5));
                Task task = taskManager.getTaskById(taskId);
                if (task != null) {
                    com.taskscheduler.ui.UIManager.displayTaskDetails(task);
                } else {
                    com.taskscheduler.ui.UIManager.displayError("Task not found with ID: " + taskId);
                }
            } else if (command.startsWith("delete ")) {
                int taskId = Integer.parseInt(command.substring(7));
                Task task = taskManager.getTaskById(taskId);
                if (task != null) {
                    taskManager.deleteTask(taskId);
                    com.taskscheduler.ui.UIManager.displaySuccess("Task deleted: \"" + task.getTitle() + "\"");
                } else {
                    com.taskscheduler.ui.UIManager.displayError("Task not found with ID: " + taskId);
                }
            } else if (command.startsWith("complete ")) {
                int taskId = Integer.parseInt(command.substring(9));
                Task task = taskManager.getTaskById(taskId);
                if (task != null) {
                    completeTask(taskId);
                    com.taskscheduler.ui.UIManager.displaySuccess("Task completed: \"" + task.getTitle() + "\"");
                } else {
                    com.taskscheduler.ui.UIManager.displayError("Task not found with ID: " + taskId);
                }
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
            } else if (command.equalsIgnoreCase("debug") || command.equalsIgnoreCase("unicode-info")) {
                // Show Unicode support and terminal information
                com.taskscheduler.ui.UIManager.displayInfo("Terminal and Unicode Information:");
                System.out.println(com.taskscheduler.ui.Icons.getUnicodeInfo());
                System.out.println("Test Unicode Characters:");
                System.out.println("  Box: " + com.taskscheduler.ui.Icons.TOP_LEFT + com.taskscheduler.ui.Icons.HORIZONTAL + com.taskscheduler.ui.Icons.TOP_RIGHT);
                System.out.println("  Emojis: " + com.taskscheduler.ui.Icons.SUCCESS + " " + com.taskscheduler.ui.Icons.CLOCK + " " + com.taskscheduler.ui.Icons.PRIORITY_HIGH);
                System.out.println("  Icons: " + com.taskscheduler.ui.Icons.COMPLETED + " " + com.taskscheduler.ui.Icons.PENDING + " " + com.taskscheduler.ui.Icons.WARNING);
            } else {
                com.taskscheduler.ui.UIManager.displayError("Unknown command. Type 'help' for available commands.");
            }
        } catch (NumberFormatException e) {
            com.taskscheduler.ui.UIManager.displayError("Invalid task ID. Please enter a valid number.");
        } catch (IllegalArgumentException e) {
            com.taskscheduler.ui.UIManager.displayError("Invalid argument: " + e.getMessage());
        } catch (DateTimeException e) {
            com.taskscheduler.ui.UIManager.displayError("Date error: " + e.getMessage());
        } catch (RuntimeException e) {
            com.taskscheduler.ui.UIManager.displayError("Runtime error: " + e.getMessage());
        }
    }    private void listUpcomingTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> upcomingTasks = taskManager.getTasks().stream()
            .filter(task -> !task.isCompleted() && task.getDueDate() != null && 
                (task.getDueDate().isAfter(now) || 
                 task.getDueDate().toLocalDate().equals(now.toLocalDate())))
            .sorted((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()))
            .collect(Collectors.toList());

        System.out.println(com.taskscheduler.ui.Banner.createSubHeader("Upcoming Tasks", com.taskscheduler.ui.Icons.UPCOMING));
        com.taskscheduler.ui.UIManager.displayTasksTable(upcomingTasks);
    }

    private void listOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskManager.getTasks().stream()
            .filter(task -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isBefore(now))
            .sorted((t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()))
            .collect(Collectors.toList());

        System.out.println(com.taskscheduler.ui.Banner.createSubHeader("Overdue Tasks", com.taskscheduler.ui.Icons.OVERDUE));
        com.taskscheduler.ui.UIManager.displayTasksTable(overdueTasks);
    }

    private void listTasksByTag(String tag) {
        List<Task> taggedTasks = taskManager.getTasks().stream()
            .filter(task -> task.hasTag(tag))
            .collect(Collectors.toList());
            
        System.out.println(com.taskscheduler.ui.Banner.createSubHeader("Tasks with tag: " + tag, com.taskscheduler.ui.Icons.TAG));
        com.taskscheduler.ui.UIManager.displayTasksTable(taggedTasks);
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



    private void completeTask(int taskId) {
        taskManager.completeTask(taskId);
    }

    private void setDueDate(int taskId, String dueDateStr) {
        LocalDateTime dueDate = null;

        // Try each formatter until one works
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                dueDate = LocalDateTime.parse(dueDateStr, formatter);
                // Validate the date components
                validateDate(dueDate);
                break;
            } catch (DateTimeParseException e) {
                // Continue with next formatter
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
    }    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime result = LocalDateTime.parse(dateStr.trim(), formatter);
                
                // Special handling for time-only format (HH:mm)
                // If the parsed time is in the past, assume it's for tomorrow
                if (dateStr.trim().matches("\\d{1,2}:\\d{2}") && result.isBefore(LocalDateTime.now())) {
                    result = result.plusDays(1);
                }
                
                return result;
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        throw new DateTimeParseException("Invalid date format", dateStr, 0);
    }

    private void handleAddTask(String taskTitle, String[] parts) {
        try {
            // Extract title and time/date
            String title;
            String timeOrDate;
            
            // Check if this is a "due" format or "at" format
            if (taskTitle.contains(" due ")) {
                String[] dueParts = taskTitle.split(" due ", 2);
                if (dueParts.length != 2) {
                    System.out.println("Invalid format. Use: add \"Task Title\" due yyyy-MM-dd HH:mm [options]");
                    return;
                }
                title = dueParts[0];
                timeOrDate = dueParts[1];
            } else if (taskTitle.contains(" at ")) {
                String[] atParts = taskTitle.split(" at ", 2);
                if (atParts.length != 2) {
                    System.out.println("Invalid format. Use: add \"Task Title\" at HH:mm [options]");
                    return;
                }
                title = atParts[0];
                timeOrDate = atParts[1];
            } else {
                System.out.println("Invalid format. Use: add \"Task Title\" at HH:mm [options]");
                return;
            }

            if (title.startsWith("\"") && title.endsWith("\"")) {
                title = title.substring(1, title.length() - 1);
            }

            LocalDateTime dueDate;
            try {
                dueDate = parseDate(timeOrDate);
            } catch (DateTimeParseException e) {
                // If timeOrDate is just a time (HH:mm), add today's date
                if (timeOrDate.matches("\\d{1,2}:\\d{2}")) {
                    String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    dueDate = parseDate(today + " " + timeOrDate);
                } else {
                    throw e;
                }
            }
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
                    }                } else if (part.startsWith("--email ")) {
                    String email = part.substring("--email ".length()).trim();
                    if (isValidEmail(email)) {
                        task.setEmail(email);
                    }
                } else if (part.equals("--notify-email")) {
                    // Get the default email from config and set it for this task
                    String defaultEmail = ConfigManager.getEmail();
                    if (defaultEmail != null && !defaultEmail.isEmpty()) {
                        task.setEmail(defaultEmail);
                        System.out.println("Email notification will be sent to: " + defaultEmail);
                    } else {
                        System.out.println("Warning: Email notification requested but no default email is set.");
                        System.out.println("Use 'email-notification <your-email>' to set a default email address.");
                    }                } else if (part.startsWith("--priority ")) {
                    String priorityStr = part.substring("--priority ".length()).trim();
                    try {
                        Priority priority = Priority.valueOf(priorityStr.toUpperCase());
                        task.setPriority(priority);
                        System.out.println("Setting priority: " + priority.getDisplayName());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid priority: " + priorityStr + ". Valid values: low, medium, high, urgent, critical");
                    }
                } else if (part.startsWith("--") && !part.contains(" ")) {
                    // Handle standalone tags like --work, --personal, --urgent, etc.
                    String tag = part.substring(2); // Remove the --
                    if (!tag.isEmpty() && !isKnownParameter(tag)) {
                        task.addTag(tag);
                        System.out.println("Adding tag: " + tag);
                    }
                }
            }

            // Verify command was set
            if (task.getCommand() == null || task.getCommand().trim().isEmpty()) {
                System.out.println("Warning: No command specified for task: " + title);
            }            taskManager.addTask(task);
            com.taskscheduler.ui.UIManager.displaySuccess("Added task: \"" + title + "\"");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format. Use: yyyy-MM-dd HH:mm or HH:mm");
        } catch (DateTimeException e) {
            System.out.println("Date error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error adding task: " + e.getMessage());
        }
    }

    /**
     * Checks if the input is already a structured command (not natural language)
     */
    private boolean isStructuredCommand(String input) {
        String trimmed = input.trim().toLowerCase();        // Commands that start with these keywords are likely structured commands
        String[] commandPrefixes = {
            "add \"", "list", "complete ", "delete ", "help", "exit", 
            "due ", "recurring ", "email-notification ", "suggestions", "clear", "refresh", "cls"
        };
        
        for (String prefix : commandPrefixes) {
            if (trimmed.startsWith(prefix)) {
                return true;
            }
        }
        
        // Additional pattern checks for structured commands
        // Pattern: add "title" at/due time/date [options]
        if (trimmed.matches("add\\s+\"[^\"]+\"\\s+(at|due)\\s+.*")) {
            return true;
        }
        
        return false;
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
        help.append("  untag <id> <tag1> [tag2 tag3 ...] - Remove tags from a task\n");        help.append("  reminder <id> <time>            - Set reminder for a task (e.g., 30m or 2h)\n");
        help.append("  email-notification <email>      - Set email for task reminders\n");
        help.append("  suggestions                     - Toggle smart command suggestions\n");
        help.append("  clear/refresh/cls               - Clear the screen and refresh display\n");
        help.append("  help                            - Show this help message\n");
        help.append("  exit                            - Exit the program");
        System.out.println(help.toString());
    }
    
    /**
     * Checks if a parameter name is a known system parameter (not a tag)
     */
    private boolean isKnownParameter(String param) {
        String[] knownParams = {
            "notify-email", "repeat", "end", "reminder", "email", "priority", "tag"
        };
        
        for (String known : knownParams) {
            if (param.equals(known)) {
                return true;
            }
        }        return false;
    }
    
    /**
     * Clears the terminal screen
     */
    private void clearScreen() {
        try {
            String operatingSystem = System.getProperty("os.name");
            
            if (operatingSystem.contains("Windows")) {
                // For Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/Mac
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
              // Print the application header again
            System.out.println();
            System.out.println(com.taskscheduler.ui.Banner.createWelcomeBanner());
            System.out.println();
        } catch (Exception e) {
            // If the above methods don't work, try using ANSI escape codes
            System.out.print("\033[H\033[2J");
            System.out.flush();
            
            // Print some newlines as a fallback
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
              // Print the application header again
            System.out.println(com.taskscheduler.ui.Banner.createWelcomeBanner());
            System.out.println();
        }
    }

    private void handleInteractiveMenu() {
        try {
            // Use our enhanced interactive UI
            String selection = com.taskscheduler.ui.InteractiveUI.showMainMenu();
            
            if (selection.contains("List all tasks")) {
                handleCommands("list");
            } 
            else if (selection.contains("List upcoming tasks")) {
                handleCommands("list upcoming");
            }
            else if (selection.contains("List overdue tasks")) {
                handleCommands("list overdue");
            }
            else if (selection.contains("Add new task")) {
                String[] taskData = com.taskscheduler.ui.InteractiveUI.createNewTaskInteractive();
                
                // Format the command based on the interactive input
                StringBuilder cmd = new StringBuilder("add \"");
                cmd.append(taskData[0]).append("\""); // Title
                
                if (!taskData[1].isEmpty()) {
                    cmd.append(" due ").append(taskData[1]); // Due date
                }
                
                cmd.append(" --priority ").append(taskData[2].toLowerCase()); // Priority
                
                // Add tags if any
                if (!taskData[3].isEmpty()) {
                    String[] tags = taskData[3].split(",");
                    for (String tag : tags) {
                        cmd.append(" --tag ").append(tag.trim());
                    }
                }
                
                // Execute the command
                handleCommands(cmd.toString());
                com.taskscheduler.ui.InteractiveUI.notify("Task created successfully!", false);
            }
            else if (selection.contains("Complete a task")) {
                // Show all tasks first
                handleCommands("list");
                
                // Get task ID to complete
                String taskIdStr = org.beryx.textio.TextIoFactory.getTextIO().newStringInputReader()
                        .withPattern("\\d+")
                        .read("\nEnter the ID of the task to complete");
                
                handleCommands("complete " + taskIdStr);
                com.taskscheduler.ui.InteractiveUI.notify("Task marked as complete!", false);
            }
            else if (selection.contains("Delete a task")) {
                // Show all tasks first
                handleCommands("list");
                
                // Get task ID to delete
                String taskIdStr = org.beryx.textio.TextIoFactory.getTextIO().newStringInputReader()
                        .withPattern("\\d+")
                        .read("\nEnter the ID of the task to delete");
                
                // Confirm deletion
                if (com.taskscheduler.ui.InteractiveUI.confirm("Are you sure you want to delete task #" + taskIdStr + "?")) {
                    handleCommands("delete " + taskIdStr);
                    com.taskscheduler.ui.InteractiveUI.notify("Task deleted successfully!", false);
                }
            }
            else if (selection.contains("Settings")) {
                handleCommands("settings");
            }
            else if (selection.contains("Help")) {
                showHelp();
            }
            // Exit option returns to normal command mode
            
        } catch (Exception e) {
            System.out.println("Error in interactive menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
