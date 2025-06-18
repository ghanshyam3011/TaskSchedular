package com.taskscheduler.nlp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.taskscheduler.util.SilentNattyParser;

/**
 * Utility class for parsing natural language date expressions using Natty library.
 */
public class DateTimeParser {
    private static final Logger logger = Logger.getLogger(DateTimeParser.class.getName());
    private final SilentNattyParser parser;
    
    static {
        // Static initialization block to ensure this is set very early
        // Set system properties to silence SLF4J and other logging frameworks
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.log.com.joestelmach", "error");
        
        // Completely silence Natty parser output
        Logger nattyLogger = Logger.getLogger("com.joestelmach.natty.Parser");
        nattyLogger.setLevel(Level.OFF);
        nattyLogger.setUseParentHandlers(false);
        
        // Silence all related loggers
        Logger.getLogger("com.joestelmach").setLevel(Level.OFF);
        Logger.getLogger("com.joestelmach.natty").setLevel(Level.OFF);
        Logger.getLogger("net.objectlab").setLevel(Level.OFF); 
        Logger.getLogger("org.antlr").setLevel(Level.OFF);
    }

    public DateTimeParser() {
        this.parser = new SilentNattyParser();
    }    /**
     * Parses natural language date/time expressions from the input text.
     * 
     * @param text The text containing date/time expressions
     * @return A list of LocalDateTime objects extracted from the text, or an empty list if none found
     */
    public List<LocalDateTime> parseDates(String text) {
        List<LocalDateTime> results = new ArrayList<>();
        
        try {
            // Use the SilentNattyParser to get dates directly without logging
            List<Date> dates = parser.parseDates(text);
            
            for (Date date : dates) {
                LocalDateTime localDateTime = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                results.add(localDateTime);
                logger.fine(() -> "Parsed date: " + localDateTime);
            }
        } catch (Exception e) {
            logger.warning(() -> "Error parsing dates from text: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Checks if the input text contains any date/time expressions.
     * 
     * @param text The text to check
     * @return true if the text contains date/time expressions, false otherwise
     */
    public boolean containsDateTimeExpressions(String text) {
        return !parseDates(text).isEmpty();
    }

    /**
     * Parses natural language date expressions from the input text and returns only the date part.
     * 
     * @param text The text containing date expressions
     * @return A list of LocalDate objects extracted from the text, or an empty list if none found
     */
    public List<LocalDate> parseDateOnly(String text) {
        List<LocalDateTime> dateTimes = parseDates(text);
        List<LocalDate> results = new ArrayList<>();
        
        for (LocalDateTime dateTime : dateTimes) {
            results.add(dateTime.toLocalDate());
        }
        
        return results;
    }
    
    /**
     * Extracts the first date/time found in the text, or null if none found.
     * 
     * @param text The text to parse
     * @return The first LocalDateTime found, or null
     */
    public LocalDateTime extractFirstDateTime(String text) {
        List<LocalDateTime> dates = parseDates(text);
        return dates.isEmpty() ? null : dates.get(0);
    }
}
