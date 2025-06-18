package com.taskscheduler.ui;

/**
 * Utility class for creating beautiful banners and headers with ASCII fallbacks
 */
public class Banner {    // Check if terminal supports Unicode
    private static final boolean SUPPORTS_UNICODE = checkUnicodeSupport();
    
    private static boolean checkUnicodeSupport() {
        String encoding = System.getProperty("file.encoding", "").toLowerCase();
        String term = System.getenv("TERM");
        String os = System.getProperty("os.name").toLowerCase();
        
        if (encoding.contains("utf")) {
            return true;
        }
        
        if (term != null && (term.contains("xterm") || term.contains("color"))) {
            return true;
        }
        
        if (os.contains("windows")) {
            return "true".equals(System.getProperty("unicode"));
        }
        
        return true;
    }
    
    public static String createWelcomeBanner() {
        StringBuilder sb = new StringBuilder();
        sb.append(Colors.CYAN_BOLD).append("\n");
        
        if (SUPPORTS_UNICODE) {
            sb.append("╔════════════════════════════════════════════════════════════════╗\n");
            sb.append("║                                                                ║\n");
            sb.append("║  ").append(Colors.YELLOW_BOLD).append("🚀 TASK SCHEDULER v1.0").append(Colors.CYAN_BOLD).append("                                     ║\n");
            sb.append("║                                                                ║\n");
            sb.append("║  ").append(Colors.GREEN).append("Your intelligent task management companion").append(Colors.CYAN_BOLD).append("                 ║\n");
            sb.append("║                                                                ║\n");
            sb.append("╚════════════════════════════════════════════════════════════════╝").append(Colors.RESET).append("\n");
        } else {
            sb.append("+================================================================+\n");
            sb.append("|                                                                |\n");
            sb.append("|  ").append(Colors.YELLOW_BOLD).append(">> TASK SCHEDULER v1.0").append(Colors.CYAN_BOLD).append("                                     |\n");
            sb.append("|                                                                |\n");
            sb.append("|  ").append(Colors.GREEN).append("Your intelligent task management companion").append(Colors.CYAN_BOLD).append("                 |\n");
            sb.append("|                                                                |\n");
            sb.append("+================================================================+").append(Colors.RESET).append("\n");
        }
        return sb.toString();
    }
    
    public static String createSectionHeader(String title) {
        StringBuilder sb = new StringBuilder();
        String decoratedTitle = " " + title + " ";
        int totalWidth = 60;
        int padding = (totalWidth - decoratedTitle.length()) / 2;
        
        sb.append("\n").append(Colors.BLUE_BOLD);
        sb.append("═".repeat(totalWidth)).append("\n");
        sb.append(" ".repeat(padding)).append(Colors.YELLOW_BOLD).append(decoratedTitle).append(Colors.BLUE_BOLD);
        sb.append(" ".repeat(totalWidth - padding - decoratedTitle.length())).append("\n");
        sb.append("═".repeat(totalWidth)).append(Colors.RESET).append("\n");
        
        return sb.toString();
    }
      public static String createSubHeader(String title, String icon) {
        String separator = SUPPORTS_UNICODE ? "─" : "-";
        return "\n" + Colors.PURPLE_BOLD + icon + " " + title + Colors.RESET + "\n" + 
               Colors.PURPLE + separator.repeat(title.length() + 3) + Colors.RESET + "\n";
    }
    
    public static String createBox(String content, String borderColor) {
        String[] lines = content.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, stripAnsiCodes(line).length());
        }
        
        StringBuilder sb = new StringBuilder();
        String topLeft = SUPPORTS_UNICODE ? "┌" : "+";
        String topRight = SUPPORTS_UNICODE ? "┐" : "+";
        String bottomLeft = SUPPORTS_UNICODE ? "└" : "+";
        String bottomRight = SUPPORTS_UNICODE ? "┘" : "+";
        String horizontal = SUPPORTS_UNICODE ? "─" : "-";
        String vertical = SUPPORTS_UNICODE ? "│" : "|";
        
        sb.append(borderColor).append(topLeft).append(horizontal.repeat(maxWidth + 2)).append(topRight).append(Colors.RESET).append("\n");
        
        for (String line : lines) {
            int padding = maxWidth - stripAnsiCodes(line).length();
            sb.append(borderColor).append(vertical).append(" ").append(Colors.RESET)
              .append(line).append(" ".repeat(padding)).append(" ")
              .append(borderColor).append(vertical).append(Colors.RESET).append("\n");
        }
        
        sb.append(borderColor).append(bottomLeft).append(horizontal.repeat(maxWidth + 2)).append(bottomRight).append(Colors.RESET);
        return sb.toString();
    }
    
    public static String createProgressBar(int current, int total, int width) {
        double percentage = (double) current / total;
        int filled = (int) (percentage * width);
        int empty = width - filled;
        
        StringBuilder sb = new StringBuilder();
        sb.append(Colors.GREEN_BACKGROUND).append(" ".repeat(filled)).append(Colors.RESET);
        sb.append(Colors.WHITE_BACKGROUND).append(" ".repeat(empty)).append(Colors.RESET);
        sb.append(String.format(" %d/%d (%.1f%%)", current, total, percentage * 100));
        
        return sb.toString();
    }
    
    public static String createStatusBadge(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return Colors.GREEN_BACKGROUND + Colors.BLACK + " ✓ COMPLETED " + Colors.RESET;
            case "overdue":
                return Colors.RED_BACKGROUND + Colors.WHITE + " ⚠ OVERDUE " + Colors.RESET;
            case "due soon":
                return Colors.YELLOW_BACKGROUND + Colors.BLACK + " ⏰ DUE SOON " + Colors.RESET;
            case "upcoming":
                return Colors.BLUE_BACKGROUND + Colors.WHITE + " 📅 UPCOMING " + Colors.RESET;
            default:
                return Colors.WHITE_BACKGROUND + Colors.BLACK + " " + status.toUpperCase() + " " + Colors.RESET;
        }
    }
    
    public static String createMenu(String[] options, int selectedIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < options.length; i++) {
            if (i == selectedIndex) {
                sb.append(Colors.YELLOW_BACKGROUND).append(Colors.BLACK)
                  .append(" ▶ ").append(options[i]).append(" ").append(Colors.RESET);
            } else {
                sb.append(Colors.WHITE).append("   ").append(options[i]).append(Colors.RESET);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private static String stripAnsiCodes(String text) {
        return text.replaceAll("\033\\[[0-9;]*m", "");
    }
}
