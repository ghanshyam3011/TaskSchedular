package com.taskscheduler.ui;

/**
 * Icons and symbols for CLI appearance (ASCII only)
 */
public class Icons {
    // Task status icons
    public static final String COMPLETED = "[DONE]";
    public static final String PENDING = "[WAIT]";
    public static final String OVERDUE = "[LATE]";
    public static final String DUE_SOON = "[DUE!]";
    public static final String UPCOMING = "[TODO]";
    public static final String RECURRING = "[RPT]";
    
    // Action icons
    public static final String ADD = "[ADD]";
    public static final String DELETE = "[DEL]";
    public static final String EDIT = "[EDIT]";
    public static final String VIEW = "[VIEW]";
    public static final String SEARCH = "[FIND]";
    public static final String SETTINGS = "[SET]";
    public static final String HELP = "[HELP]";
    public static final String EXIT = "[EXIT]";
    
    // General icons
    public static final String CLOCK = "[TIME]";
    public static final String CALENDAR = "[CAL]";
    public static final String EMAIL = "[MAIL]";
    public static final String COMMAND = "[CMD]";
    public static final String TAG = "[TAG]";
    public static final String REMINDER = "[RING]";
    public static final String SUCCESS = "[OK]";
    public static final String ERROR = "[ERR]";
    public static final String WARNING = "[WARN]";
    public static final String INFO = "[INFO]";
    
    // Navigation icons
    public static final String ARROW_RIGHT = ">";
    public static final String ARROW_LEFT = "<";
    public static final String ARROW_UP = "^";
    public static final String ARROW_DOWN = "v";
    public static final String BULLET = "*";
    public static final String CHEVRON_RIGHT = ">";
    public static final String CHEVRON_DOWN = "v";
    
    // Border characters for tables
    public static final String HORIZONTAL = "-";
    public static final String VERTICAL = "|";
    public static final String TOP_LEFT = "+";
    public static final String TOP_RIGHT = "+";
    public static final String BOTTOM_LEFT = "+";
    public static final String BOTTOM_RIGHT = "+";
    public static final String CROSS = "+";
    public static final String T_DOWN = "+";
    public static final String T_UP = "+";
    public static final String T_RIGHT = "+";
    public static final String T_LEFT = "+";
    
    // Progress indicators
    public static final String PROGRESS_FULL = "#";
    public static final String PROGRESS_PARTIAL = "-";
    public static final String PROGRESS_EMPTY = ".";
    public static final String SPINNER = "|/-\\";
    
    // Status indicators
    public static final String ONLINE = "[O]";
    public static final String OFFLINE = "[X]";
    public static final String SYNC = "[S]";
    public static final String STAR = "[*]";
    public static final String FLAG = "[F]";
    
    // Productivity icons
    public static final String TASK = "[T]";
    public static final String PROJECT = "[P]";
    public static final String DEADLINE = "[D]";
    public static final String PRIORITY_HIGH = "[H]";
    public static final String PRIORITY_MEDIUM = "[M]";
    public static final String PRIORITY_LOW = "[L]";
    
    /**
     * Get Unicode support status
     */
    public static boolean supportsUnicode() {
        return false;
    }
    
    /**
     * Get detailed Unicode support information for debugging
     */
    public static String getUnicodeInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Unicode Support: false").append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append("\n");
        info.append("File Encoding: ").append(System.getProperty("file.encoding")).append("\n");
        info.append("TERM: ").append(System.getenv("TERM")).append("\n");
        info.append("WT_SESSION: ").append(System.getenv("WT_SESSION")).append("\n");
        info.append("VSCODE_INJECTION: ").append(System.getenv("VSCODE_INJECTION")).append("\n");
        info.append("CONSOLE_CP: ").append(System.getenv("CONSOLE_CP")).append("\n");
        info.append("Unicode Property: ").append(System.getProperty("unicode")).append("\n");
        return info.toString();
    }
}
