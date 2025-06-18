package com.taskscheduler.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Enhanced UI Manager for beautiful task display and interaction
 */
public class UIManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public static void displayWelcome() {
        clearScreen();
        System.out.println(Banner.createWelcomeBanner());
        displayQuickStats();
    }
    
    public static void displayQuickStats() {
        // This will be implemented after we update TaskManager to provide stats
        System.out.println(Colors.info("💡 Type 'help' for available commands or 'menu' for interactive mode"));
        System.out.println();
    }
    
    public static void displayTasksTable(List<com.taskscheduler.Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println(Banner.createSubHeader("No tasks found", Icons.INFO));
            System.out.println(Colors.warning("📝 Use 'add' command to create your first task!"));
            return;
        }
        
        System.out.println(Banner.createSubHeader("Task Overview", Icons.TASK));
          Table table = new Table()
            .setHeaders("ID", "Priority", "Status", "Task", "Due Date", "Email", "Tags")
            .setBorderColor(Colors.CYAN)
            .setHeaderColor(Colors.BLUE_BOLD)
            .setDataColor(Colors.WHITE);
          for (com.taskscheduler.Task task : tasks) {
            String statusIcon = getTaskStatusIcon(task);
            String statusText = getTaskStatusText(task);
            String priorityDisplay = task.getPriority().getColoredDisplay();
            String dueDate = task.getDueDate() != null ? 
                task.getDueDate().format(DATE_FORMATTER) : "No due date";
            String emailStatus = task.getEmail() != null ? 
                Colors.success("✓") : Colors.error("✗");
            String tags = task.getTags().isEmpty() ? 
                Colors.DIM + "none" + Colors.RESET : 
                String.join(", ", task.getTags());
            
            table.addRow(
                String.valueOf(task.getId()),
                priorityDisplay,
                statusIcon + " " + statusText,
                truncateText(task.getTitle(), 25),
                dueDate,
                emailStatus,
                truncateText(tags, 15)
            );
        }
        
        System.out.println(table.render());
        displayTaskSummary(tasks);
    }
    
    public static void displayTaskDetails(com.taskscheduler.Task task) {
        System.out.println(Banner.createSubHeader("Task Details", Icons.VIEW));
        
        StringBuilder details = new StringBuilder();        details.append(Colors.BLUE_BOLD).append("ID: ").append(Colors.RESET).append(task.getId()).append("\n");
        details.append(Colors.BLUE_BOLD).append("Title: ").append(Colors.RESET).append(task.getTitle()).append("\n");
        details.append(Colors.BLUE_BOLD).append("Priority: ").append(Colors.RESET)
               .append(task.getPriority().getColoredDisplay()).append("\n");
        details.append(Colors.BLUE_BOLD).append("Status: ").append(Colors.RESET)
               .append(getTaskStatusIcon(task)).append(" ").append(getTaskStatusText(task)).append("\n");
        
        if (task.getDueDate() != null) {
            details.append(Colors.BLUE_BOLD).append("Due Date: ").append(Colors.RESET)
                   .append(task.getDueDate().format(DATE_FORMATTER)).append("\n");
            details.append(Colors.BLUE_BOLD).append("Time Until Due: ").append(Colors.RESET)
                   .append(getTimeUntilDue(task)).append("\n");
        }
        
        details.append(Colors.BLUE_BOLD).append("Email Notification: ").append(Colors.RESET)
               .append(task.getEmail() != null ? Colors.success("Enabled (" + task.getEmail() + ")") : Colors.error("Disabled")).append("\n");
        
        if (task.getCommand() != null && !task.getCommand().isEmpty()) {
            details.append(Colors.BLUE_BOLD).append("Command: ").append(Colors.RESET)
                   .append(Colors.CYAN).append(task.getCommand()).append(Colors.RESET).append("\n");
        }
        
        if (!task.getTags().isEmpty()) {
            details.append(Colors.BLUE_BOLD).append("Tags: ").append(Colors.RESET)
                   .append(Colors.YELLOW).append(String.join(", ", task.getTags())).append(Colors.RESET).append("\n");
        }
        
        if (task.isRecurring()) {
            details.append(Colors.BLUE_BOLD).append("Recurring: ").append(Colors.RESET)
                   .append(Colors.PURPLE).append(task.getRecurrenceType()).append(Colors.RESET).append("\n");
        }
        
        System.out.println(Banner.createBox(details.toString().trim(), Colors.CYAN));
    }
    
    public static void displaySuccess(String message) {
        System.out.println(Colors.success(Icons.SUCCESS + " " + message));
    }
    
    public static void displayError(String message) {
        System.out.println(Colors.error(Icons.ERROR + " " + message));
    }
    
    public static void displayWarning(String message) {
        System.out.println(Colors.warning(Icons.WARNING + " " + message));
    }
    
    public static void displayInfo(String message) {
        System.out.println(Colors.info(Icons.INFO + " " + message));
    }
    
    public static void displayPrompt(String prompt) {
        System.out.print(Colors.CYAN_BOLD + "> " + Colors.RESET + prompt);
    }
    
    public static void displayCommandPrompt() {
        System.out.print(Colors.YELLOW_BOLD + "📋 TaskScheduler" + Colors.CYAN + " > " + Colors.RESET);
    }
      private static void displayTaskSummary(List<com.taskscheduler.Task> tasks) {
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(com.taskscheduler.Task::isCompleted).count();
        int overdue = (int) tasks.stream().filter(UIManager::isOverdue).count();
        int dueSoon = (int) tasks.stream().filter(UIManager::isDueSoon).count();
        
        System.out.println("\n" + Colors.CYAN_BOLD + "📊 Summary:" + Colors.RESET);
        System.out.println(String.format("  Total: %s%d%s  |  Completed: %s%d%s  |  Overdue: %s%d%s  |  Due Soon: %s%d%s",
            Colors.WHITE_BOLD, total, Colors.RESET,
            Colors.GREEN_BOLD, completed, Colors.RESET,
            Colors.RED_BOLD, overdue, Colors.RESET,
            Colors.YELLOW_BOLD, dueSoon, Colors.RESET
        ));
        
        if (total > 0) {
            System.out.println("  Progress: " + Banner.createProgressBar(completed, total, 20));
        }
        System.out.println();
    }
    
    private static String getTaskStatusIcon(com.taskscheduler.Task task) {
        if (task.isCompleted()) {
            return Icons.COMPLETED;
        } else if (isOverdue(task)) {
            return Icons.OVERDUE;
        } else if (isDueSoon(task)) {
            return Icons.DUE_SOON;
        } else {
            return Icons.UPCOMING;
        }
    }
    
    private static String getTaskStatusText(com.taskscheduler.Task task) {
        if (task.isCompleted()) {
            return Colors.success("Completed");
        } else if (isOverdue(task)) {
            return Colors.error("Overdue");
        } else if (isDueSoon(task)) {
            return Colors.warning("Due Soon");
        } else {
            return Colors.info("Upcoming");
        }
    }
    
    private static boolean isOverdue(com.taskscheduler.Task task) {
        return task.getDueDate() != null && !task.isCompleted() && 
               task.getDueDate().isBefore(LocalDateTime.now());
    }
    
    private static boolean isDueSoon(com.taskscheduler.Task task) {
        if (task.getDueDate() == null || task.isCompleted()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = task.getDueDate();
        return dueDate.isAfter(now) && ChronoUnit.HOURS.between(now, dueDate) <= 24;
    }
    
    private static String getTimeUntilDue(com.taskscheduler.Task task) {
        if (task.getDueDate() == null) {
            return "No due date set";
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = task.getDueDate();
        
        if (dueDate.isBefore(now)) {
            long hoursOverdue = ChronoUnit.HOURS.between(dueDate, now);
            return Colors.error("Overdue by " + hoursOverdue + " hours");
        } else {
            long hoursUntilDue = ChronoUnit.HOURS.between(now, dueDate);
            if (hoursUntilDue < 24) {
                return Colors.warning(hoursUntilDue + " hours");
            } else {
                long daysUntilDue = ChronoUnit.DAYS.between(now, dueDate);
                return Colors.info(daysUntilDue + " days");
            }
        }
    }
    
    private static String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    public static void clearScreen() {
        System.out.print(Colors.CLEAR_SCREEN);
        System.out.flush();
    }
    
    public static void displayHelp() {
        System.out.println(Banner.createSubHeader("Available Commands", Icons.HELP));
        
        Table helpTable = new Table()
            .setHeaders("Command", "Description", "Example")
            .setBorderColor(Colors.GREEN)
            .setHeaderColor(Colors.GREEN_BOLD);
        
        helpTable.addRow("add", "Create a new task", "add \"Meeting\" due 2025-06-18 14:00");
        helpTable.addRow("list", "Show all tasks", "list");
        helpTable.addRow("list upcoming", "Show upcoming tasks", "list upcoming");
        helpTable.addRow("list overdue", "Show overdue tasks", "list overdue");
        helpTable.addRow("complete <id>", "Mark task as completed", "complete 1");
        helpTable.addRow("delete <id>", "Delete a task", "delete 2");
        helpTable.addRow("view <id>", "Show task details", "view 1");
        helpTable.addRow("email-notification", "Set email for notifications", "email-notification user@example.com");
        helpTable.addRow("test-email", "Test email system", "test-email");
        helpTable.addRow("menu", "Interactive menu mode", "menu");
        helpTable.addRow("help", "Show this help", "help");
        helpTable.addRow("exit", "Exit the application", "exit");
        
        System.out.println(helpTable.render());
        
        System.out.println(Colors.CYAN_BOLD + "\n💡 Pro Tips:" + Colors.RESET);
        System.out.println(Colors.WHITE + "  • Use natural language: " + Colors.YELLOW + "\"remind me to call John tomorrow at 3pm and email me\"" + Colors.RESET);
        System.out.println(Colors.WHITE + "  • Add email notifications: " + Colors.YELLOW + "\"--notify-email\"" + Colors.RESET);
        System.out.println(Colors.WHITE + "  • Set custom commands: " + Colors.YELLOW + "\"--command 'echo Task completed'\"" + Colors.RESET);
        System.out.println();
    }
}
