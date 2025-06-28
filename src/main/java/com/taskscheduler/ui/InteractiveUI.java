package com.taskscheduler.ui;

import com.vdurmont.emoji.EmojiParser;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Enhanced interactive UI components using Text-IO library
 * for better user interaction and input handling
 */
public class InteractiveUI {
    private static final TextIO textIO = TextIoFactory.getTextIO();
    private static final TextTerminal<?> terminal = textIO.getTextTerminal();
    
    /**
     * Display an enhanced welcome message with animation
     */
    public static void showWelcome() {
        clearScreen();
        
        // Animated welcome
        String[] welcomeFrames = {
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                                                                ║\n" +
            "║  🚀 TASK SCHEDULER v1.0                                     ║\n" +
            "║                                                                ║\n" +
            "║  Your intelligent task management companion                    ║\n" +
            "║                                                                ║\n" +
            "╚════════════════════════════════════════════════════════════════╝",
            
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                                                                ║\n" +
            "║  ⏳ TASK SCHEDULER v1.0                                     ║\n" +
            "║                                                                ║\n" +
            "║  Your intelligent task management companion                    ║\n" +
            "║                                                                ║\n" +
            "╚════════════════════════════════════════════════════════════════╝",
            
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                                                                ║\n" +
            "║  📅 TASK SCHEDULER v1.0                                     ║\n" +
            "║                                                                ║\n" +
            "║  Your intelligent task management companion                    ║\n" +
            "║                                                                ║\n" +
            "╚════════════════════════════════════════════════════════════════╝",
            
            "╔════════════════════════════════════════════════════════════════╗\n" +
            "║                                                                ║\n" +
            "║  🚀 TASK SCHEDULER v1.0                                     ║\n" +
            "║                                                                ║\n" +
            "║  Your intelligent task management companion                    ║\n" +
            "║                                                                ║\n" +
            "╚════════════════════════════════════════════════════════════════╝"
        };

        for (int i = 0; i < 2; i++) {
            for (String frame : welcomeFrames) {
                clearScreen();
                System.out.println(Colors.CYAN_BOLD + frame + Colors.RESET);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        System.out.println("\n" + withEmoji(":bulb: Type 'help' for available commands or 'menu' for interactive mode"));
    }
    
    /**
     * Clear the screen
     */
    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback
            for (int i = 0; i < 100; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Parse emojis in text
     */
    public static String withEmoji(String text) {
        return EmojiParser.parseToUnicode(text);
    }
    
    /**
     * Show an interactive menu for the user
     * @return The selected option
     */
    public static String showMainMenu() {
        clearScreen();
        terminal.println(Colors.CYAN_BOLD + "┌─ " + withEmoji(":rocket: Task Scheduler") + " ─┐" + Colors.RESET);
        
        List<String> options = Arrays.asList(
            withEmoji(":clipboard: List all tasks"),
            withEmoji(":calendar: List upcoming tasks"),
            withEmoji(":clock3: List overdue tasks"),
            withEmoji(":pencil: Add new task"),
            withEmoji(":white_check_mark: Complete a task"),
            withEmoji(":x: Delete a task"),
            withEmoji(":wrench: Settings"),
            withEmoji(":question: Help"),
            withEmoji(":door: Exit")
        );
        
        return textIO.newStringInputReader()
                .withNumberedPossibleValues(options)
                .read("\nSelect an option");
    }
    
    /**
     * Create a new task interactively
     * @return String array with [title, due date, priority, tags]
     */
    public static String[] createNewTaskInteractive() {
        clearScreen();
        terminal.println(Colors.CYAN_BOLD + "┌─ " + withEmoji(":pencil: Create New Task") + " ─┐" + Colors.RESET);
        
        String title = textIO.newStringInputReader()
                .withMinLength(3)
                .read("Task title");
        
        boolean hasDueDate = textIO.newBooleanInputReader()
                .withDefaultValue(true)
                .read("Set due date?");
        
        LocalDateTime dueDate = null;
        if (hasDueDate) {
            String dateStr = textIO.newStringInputReader()
                    .withDefaultValue(LocalDate.now().toString())
                    .withPattern("\\d{4}-\\d{2}-\\d{2}")
                    .read("Date (YYYY-MM-DD)");
            
            String timeStr = textIO.newStringInputReader()
                    .withDefaultValue("12:00")
                    .withPattern("\\d{1,2}:\\d{2}")
                    .read("Time (HH:MM)");
            
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            
            dueDate = LocalDateTime.of(date, time);
        }
        
        String priority = textIO.newStringInputReader()
                .withNumberedPossibleValues("HIGH", "MEDIUM", "LOW")
                .withDefaultValue("MEDIUM")
                .read("Priority");
        
        String tagsInput = textIO.newStringInputReader()
                .withDefaultValue("")
                .read("Tags (comma-separated, leave empty for none)");
        
        String[] tags = tagsInput.isEmpty() ? new String[0] : tagsInput.split(",");
        
        String[] result = new String[4];
        result[0] = title;
        result[1] = dueDate != null ? dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        result[2] = priority;
        result[3] = String.join(",", tags);
        
        return result;
    }
    
    /**
     * Show a confirmation dialog
     */
    public static boolean confirm(String message) {
        return textIO.newBooleanInputReader()
                .withDefaultValue(false)
                .read(withEmoji(message));
    }
    
    /**
     * Show a notification
     */
    public static void notify(String message, boolean isError) {
        String prefix = isError ? withEmoji(":x: ") : withEmoji(":white_check_mark: ");
        String color = isError ? Colors.RED_BOLD : Colors.GREEN_BOLD;
        
        terminal.println("\n" + color + prefix + message + Colors.RESET);
        terminal.println(Colors.DIM + "Press enter to continue..." + Colors.RESET);
        textIO.newStringInputReader().withMinLength(0).read("");
    }
}
