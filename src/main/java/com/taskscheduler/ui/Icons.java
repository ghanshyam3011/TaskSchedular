package com.taskscheduler.ui;

/**
 * Unicode icons and symbols for enhanced CLI appearance with ASCII fallbacks
 */
public class Icons {    // Check if terminal supports Unicode
    private static final boolean SUPPORTS_UNICODE = checkUnicodeSupport();
    
    private static boolean checkUnicodeSupport() {
        // Check various indicators for Unicode support
        String encoding = System.getProperty("file.encoding", "").toLowerCase();
        String term = System.getenv("TERM");
        String os = System.getProperty("os.name").toLowerCase();
        
        // If encoding contains UTF, likely supports Unicode
        if (encoding.contains("utf")) {
            return true;
        }
        
        // Modern terminals usually support Unicode
        if (term != null && (term.contains("xterm") || term.contains("color"))) {
            return true;
        }
        
        // For Windows, be conservative and use ASCII by default
        // Users can override with system property -Dunicode=true
        if (os.contains("windows")) {
            return "true".equals(System.getProperty("unicode"));
        }
        
        // For other systems (Linux, Mac), assume Unicode support
        return true;
    }
    
    // Task status icons
    public static final String COMPLETED = SUPPORTS_UNICODE ? "✅" : "[✓]";
    public static final String PENDING = SUPPORTS_UNICODE ? "⏳" : "[⧗]";
    public static final String OVERDUE = SUPPORTS_UNICODE ? "❌" : "[✗]";
    public static final String DUE_SOON = SUPPORTS_UNICODE ? "⚠️" : "[!]";
    public static final String UPCOMING = SUPPORTS_UNICODE ? "📅" : "[○]";
    public static final String RECURRING = SUPPORTS_UNICODE ? "🔄" : "[↻]";
    
    // Action icons
    public static final String ADD = SUPPORTS_UNICODE ? "➕" : "[+]";
    public static final String DELETE = SUPPORTS_UNICODE ? "🗑️" : "[X]";
    public static final String EDIT = SUPPORTS_UNICODE ? "✏️" : "[E]";
    public static final String VIEW = SUPPORTS_UNICODE ? "👁️" : "[V]";
    public static final String SEARCH = SUPPORTS_UNICODE ? "🔍" : "[?]";    public static final String SETTINGS = SUPPORTS_UNICODE ? "⚙️" : "[S]";
    public static final String HELP = SUPPORTS_UNICODE ? "❓" : "[?]";
    public static final String EXIT = SUPPORTS_UNICODE ? "🚪" : "[X]";
    
    // General icons
    public static final String CLOCK = SUPPORTS_UNICODE ? "🕐" : "[T]";
    public static final String CALENDAR = SUPPORTS_UNICODE ? "📅" : "[C]";
    public static final String EMAIL = SUPPORTS_UNICODE ? "📧" : "[@]";
    public static final String COMMAND = SUPPORTS_UNICODE ? "💻" : "[>]";
    public static final String TAG = SUPPORTS_UNICODE ? "🏷️" : "[#]";
    public static final String REMINDER = SUPPORTS_UNICODE ? "🔔" : "[R]";
    public static final String SUCCESS = SUPPORTS_UNICODE ? "🎉" : "[✓]";
    public static final String ERROR = SUPPORTS_UNICODE ? "💥" : "[!]";
    public static final String WARNING = SUPPORTS_UNICODE ? "⚠️" : "[!]";
    public static final String INFO = SUPPORTS_UNICODE ? "ℹ️" : "[i]";
    
    // Navigation icons
    public static final String ARROW_RIGHT = SUPPORTS_UNICODE ? "→" : ">";
    public static final String ARROW_LEFT = SUPPORTS_UNICODE ? "←" : "<";
    public static final String ARROW_UP = SUPPORTS_UNICODE ? "↑" : "^";
    public static final String ARROW_DOWN = SUPPORTS_UNICODE ? "↓" : "v";
    public static final String BULLET = SUPPORTS_UNICODE ? "•" : "*";
    public static final String CHEVRON_RIGHT = SUPPORTS_UNICODE ? "▶" : ">";
    public static final String CHEVRON_DOWN = SUPPORTS_UNICODE ? "▼" : "v";
    
    // Border characters for tables
    public static final String HORIZONTAL = SUPPORTS_UNICODE ? "─" : "-";
    public static final String VERTICAL = SUPPORTS_UNICODE ? "│" : "|";
    public static final String TOP_LEFT = SUPPORTS_UNICODE ? "┌" : "+";
    public static final String TOP_RIGHT = SUPPORTS_UNICODE ? "┐" : "+";
    public static final String BOTTOM_LEFT = SUPPORTS_UNICODE ? "└" : "+";
    public static final String BOTTOM_RIGHT = SUPPORTS_UNICODE ? "┘" : "+";
    public static final String CROSS = SUPPORTS_UNICODE ? "┼" : "+";
    public static final String T_DOWN = SUPPORTS_UNICODE ? "┬" : "+";
    public static final String T_UP = SUPPORTS_UNICODE ? "┴" : "+";
    public static final String T_RIGHT = SUPPORTS_UNICODE ? "├" : "+";
    public static final String T_LEFT = SUPPORTS_UNICODE ? "┤" : "+";
    
    // Progress indicators
    public static final String PROGRESS_FULL = SUPPORTS_UNICODE ? "█" : "#";
    public static final String PROGRESS_PARTIAL = SUPPORTS_UNICODE ? "▓" : "-";
    public static final String PROGRESS_EMPTY = SUPPORTS_UNICODE ? "░" : ".";
    public static final String SPINNER = SUPPORTS_UNICODE ? "⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏" : "|/-\\";
    
    // Status indicators
    public static final String ONLINE = SUPPORTS_UNICODE ? "🟢" : "[O]";
    public static final String OFFLINE = SUPPORTS_UNICODE ? "🔴" : "[X]";
    public static final String SYNC = SUPPORTS_UNICODE ? "🔄" : "[S]";
    public static final String STAR = SUPPORTS_UNICODE ? "⭐" : "[*]";
    public static final String FLAG = SUPPORTS_UNICODE ? "🚩" : "[F]";
    
    // Productivity icons
    public static final String TASK = SUPPORTS_UNICODE ? "📋" : "[T]";
    public static final String PROJECT = SUPPORTS_UNICODE ? "📁" : "[P]";
    public static final String DEADLINE = SUPPORTS_UNICODE ? "⏰" : "[D]";
    public static final String PRIORITY_HIGH = SUPPORTS_UNICODE ? "🔥" : "[H]";
    public static final String PRIORITY_MEDIUM = SUPPORTS_UNICODE ? "🟡" : "[M]";
    public static final String PRIORITY_LOW = SUPPORTS_UNICODE ? "🟢" : "[L]";
}
