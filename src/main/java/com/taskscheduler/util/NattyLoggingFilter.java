package com.taskscheduler.util;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * A logging filter that blocks all log messages from Natty parser and related libraries.
 * This provides a more aggressive filtering mechanism to ensure a clean console output.
 */
public class NattyLoggingFilter implements Filter {
    
    @Override
    public boolean isLoggable(LogRecord record) {
        // Check the source class name, logger name, and message content
        String sourceClassName = record.getSourceClassName();
        String loggerName = record.getLoggerName();
        String message = record.getMessage();
        
        // Block all logs from known Natty and related classes/loggers
        if (sourceClassName != null && 
            (sourceClassName.contains("com.joestelmach") || 
             sourceClassName.contains("natty") ||
             sourceClassName.contains("net.objectlab") ||
             sourceClassName.contains("org.antlr") ||
             sourceClassName.contains("org.quartz"))) {
            return false;
        }
        
        if (loggerName != null && 
            (loggerName.contains("com.joestelmach") || 
             loggerName.contains("natty") ||
             loggerName.contains("net.objectlab") ||
             loggerName.contains("org.antlr") ||
             loggerName.contains("org.quartz"))) {
            return false;
        }
        
        // Also filter out by message content
        if (message != null &&
            (message.contains("STREAM:") || 
             message.contains("GROUP:") || 
             message.contains("PARSE:") || 
             message.contains("AST:") || 
             message.contains("ANTLR") ||
             message.contains("Quartz") ||
             message.contains("Parser"))) {
            return false;
        }
        
        // If it's not from a blocked source, allow the log
        return true;
    }
}