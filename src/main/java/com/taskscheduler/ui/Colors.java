package com.taskscheduler.ui;

/**
 * ANSI color codes and styling utilities for enhanced CLI appearance
 */
public class Colors {
    // Reset
    public static final String RESET = "\033[0m";
    
    // Regular Colors
    public static final String BLACK = "\033[0;30m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE = "\033[0;37m";
    
    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";
    public static final String RED_BOLD = "\033[1;31m";
    public static final String GREEN_BOLD = "\033[1;32m";
    public static final String YELLOW_BOLD = "\033[1;33m";
    public static final String BLUE_BOLD = "\033[1;34m";
    public static final String PURPLE_BOLD = "\033[1;35m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    
    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";
    public static final String RED_BACKGROUND = "\033[41m";
    public static final String GREEN_BACKGROUND = "\033[42m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String BLUE_BACKGROUND = "\033[44m";
    public static final String PURPLE_BACKGROUND = "\033[45m";
    public static final String CYAN_BACKGROUND = "\033[46m";
    public static final String WHITE_BACKGROUND = "\033[47m";
    
    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";
    public static final String RED_BRIGHT = "\033[0;91m";
    public static final String GREEN_BRIGHT = "\033[0;92m";
    public static final String YELLOW_BRIGHT = "\033[0;93m";
    public static final String BLUE_BRIGHT = "\033[0;94m";
    public static final String PURPLE_BRIGHT = "\033[0;95m";
    public static final String CYAN_BRIGHT = "\033[0;96m";
    public static final String WHITE_BRIGHT = "\033[0;97m";
    
    // Text formatting
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String ITALIC = "\033[3m";
    public static final String UNDERLINE = "\033[4m";
    public static final String BLINK = "\033[5m";
    public static final String REVERSE = "\033[7m";
    public static final String STRIKETHROUGH = "\033[9m";
    
    // Cursor controls
    public static final String CLEAR_SCREEN = "\033[2J";
    public static final String CLEAR_LINE = "\033[K";
    public static final String MOVE_UP = "\033[1A";
    public static final String MOVE_DOWN = "\033[1B";
    public static final String MOVE_RIGHT = "\033[1C";
    public static final String MOVE_LEFT = "\033[1D";
    
    // Utility methods
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }
    
    public static String success(String text) {
        return GREEN_BOLD + text + RESET;
    }
    
    public static String error(String text) {
        return RED_BOLD + text + RESET;
    }
    
    public static String warning(String text) {
        return YELLOW_BOLD + text + RESET;
    }
    
    public static String info(String text) {
        return CYAN_BOLD + text + RESET;
    }
    
    public static String highlight(String text) {
        return YELLOW_BACKGROUND + BLACK + " " + text + " " + RESET;
    }
    
    public static String title(String text) {
        return BLUE_BOLD + UNDERLINE + text + RESET;
    }
    
    public static String subtitle(String text) {
        return PURPLE_BOLD + text + RESET;
    }
}
