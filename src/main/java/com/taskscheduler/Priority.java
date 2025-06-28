package com.taskscheduler;

/**
 * Priority levels for tasks
 */
public enum Priority {
    LOW(1, "Low", "●"),
    MEDIUM(2, "Medium", "●"), 
    HIGH(3, "High", "●"),
    URGENT(4, "Urgent", "●"),
    CRITICAL(5, "Critical", "●");
    
    private final int level;
    private final String displayName;
    private final String icon;
    
    Priority(int level, String displayName, String icon) {
        this.level = level;
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }    public String getColoredDisplay() {
        switch (this) {
            case LOW:
                return "\033[0;32m" + icon + " " + displayName + "\033[0m"; // Green
            case MEDIUM:
                return "\033[0;33m" + icon + " " + displayName + "\033[0m"; // Yellow
            case HIGH:
                return "\033[0;35m" + icon + " " + displayName + "\033[0m"; // Magenta
            case URGENT:
                return "\033[0;31m" + icon + " " + displayName + "\033[0m"; // Red
            case CRITICAL:
                return "\033[1;31m" + icon + " " + displayName + "\033[0m"; // Bold Red
            default:
                return displayName;
        }
    }
    
    public static Priority fromString(String priorityStr) {
        if (priorityStr == null) return MEDIUM;
        
        String normalized = priorityStr.toLowerCase().trim();
        switch (normalized) {
            case "low":
            case "l":
            case "1":
                return LOW;
            case "medium":
            case "med":
            case "m":
            case "2":
                return MEDIUM;
            case "high":
            case "h":
            case "3":
                return HIGH;
            case "urgent":
            case "u":
            case "4":
                return URGENT;
            case "critical":
            case "crit":
            case "c":
            case "5":
                return CRITICAL;
            default:
                return MEDIUM;
        }
    }
      public static Priority detectFromText(String text) {
        if (text == null) return MEDIUM;
        
        String normalized = text.toLowerCase().trim();
        
        // Critical keywords - highest priority
        if (normalized.contains("critical") || normalized.contains("emergency") || 
            normalized.contains("asap") || normalized.contains("immediately") ||
            normalized.contains("right now") || normalized.contains("urgent emergency") ||
            normalized.contains("crisis") || normalized.contains("fire drill") ||
            normalized.contains("drop everything") || normalized.contains("life or death")) {
            return CRITICAL;
        }
        
        // Urgent keywords - second highest priority
        if (normalized.contains("urgent") || normalized.contains("important") ||
            normalized.contains("high priority") || normalized.contains("rush") ||
            normalized.contains("quickly") || normalized.contains("fast") ||
            normalized.contains("time sensitive") || normalized.contains("can't wait") ||
            normalized.contains("need now") || normalized.contains("pressing") ||
            normalized.contains("vital") || normalized.contains("essential") ||
            normalized.contains("must do") || normalized.contains("top priority") ||
            normalized.contains("priority 1") || normalized.contains("urgent work") ||
            normalized.contains("urgent task") || normalized.contains("urgent meeting") ||
            normalized.contains("urgent call") || normalized.contains("urgent email")) {
            return URGENT;
        }
        
        // High priority keywords - third priority level
        if (normalized.contains("high") || normalized.contains("soon") ||
            normalized.contains("deadline") || normalized.contains("due soon") ||
            normalized.contains("important work") || normalized.contains("important task") ||
            normalized.contains("important meeting") || normalized.contains("priority 2") ||
            normalized.contains("fairly urgent") || normalized.contains("somewhat urgent") ||
            normalized.contains("needs attention") || normalized.contains("significant") ||
            normalized.contains("key task") || normalized.contains("major") ||
            normalized.contains("this week") || normalized.contains("by friday")) {
            return HIGH;
        }
        
        // Low priority keywords - lowest priority
        if (normalized.contains("low") || normalized.contains("later") ||
            normalized.contains("someday") || normalized.contains("when possible") ||
            normalized.contains("if time") || normalized.contains("nice to have") ||
            normalized.contains("eventually") || normalized.contains("no rush") ||
            normalized.contains("low priority") || normalized.contains("priority 4") ||
            normalized.contains("priority 5") || normalized.contains("minor") ||
            normalized.contains("trivial") || normalized.contains("optional") ||
            normalized.contains("when free") || normalized.contains("leisure") ||
            normalized.contains("backlog") || normalized.contains("whenever")) {
            return LOW;
        }
        
        // Context-based priority detection for work/task types
        if (normalized.contains("meeting") || normalized.contains("call") || 
            normalized.contains("interview") || normalized.contains("presentation")) {
            // Meetings are typically higher priority
            return HIGH;
        }
        
        if (normalized.contains("today") && !normalized.contains("low")) {
            // Tasks for today are typically higher priority unless explicitly marked low
            return HIGH;
        }
        
        return MEDIUM; // Default
    }
}
