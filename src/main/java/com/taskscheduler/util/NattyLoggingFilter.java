package com.taskscheduler.util;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * A logging filter that blocks all log messages from Natty parser and related libraries.
 */
public class NattyLoggingFilter implements Filter {
    
    @Override
    public boolean isLoggable(LogRecord record) {
        // Check the source class name and logger name
        String sourceClassName = record.getSourceClassName();
        String loggerName = record.getLoggerName();
        
        // Block all logs from known Natty and related classes/loggers
        if (sourceClassName != null && 
            (sourceClassName.contains("com.joestelmach") || 
             sourceClassName.contains("natty") ||
             sourceClassName.contains("net.objectlab") ||
             sourceClassName.contains("org.antlr"))) {
            return false;
        }
        
        if (loggerName != null && 
            (loggerName.contains("com.joestelmach") || 
             loggerName.contains("natty") ||
             loggerName.contains("net.objectlab") ||
             loggerName.contains("org.antlr"))) {
            return false;
        }
        
        // If it's not from Natty, allow the log
        return true;
    }
}
