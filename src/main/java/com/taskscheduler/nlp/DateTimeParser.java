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

public class DateTimeParser {
    private static final Logger logger = Logger.getLogger(DateTimeParser.class.getName());
    private final SilentNattyParser parser;
    
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.log.com.joestelmach", "error");
        
        Logger nattyLogger = Logger.getLogger("com.joestelmach.natty.Parser");
        nattyLogger.setLevel(Level.OFF);
        nattyLogger.setUseParentHandlers(false);
        
        Logger.getLogger("com.joestelmach").setLevel(Level.OFF);
        Logger.getLogger("com.joestelmach.natty").setLevel(Level.OFF);
        Logger.getLogger("net.objectlab").setLevel(Level.OFF); 
        Logger.getLogger("org.antlr").setLevel(Level.OFF);
    }

    public DateTimeParser() {
        this.parser = new SilentNattyParser();
    }
    
    public List<LocalDateTime> parseDates(String text) {
        List<LocalDateTime> results = new ArrayList<>();
        
        try {
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

    public boolean containsDateTimeExpressions(String text) {
        return !parseDates(text).isEmpty();
    }

    public List<LocalDate> parseDateOnly(String text) {
        List<LocalDateTime> dateTimes = parseDates(text);
        List<LocalDate> results = new ArrayList<>();
        
        for (LocalDateTime dateTime : dateTimes) {
            results.add(dateTime.toLocalDate());
        }
        
        return results;
    }
    
    public LocalDateTime extractFirstDateTime(String text) {
        List<LocalDateTime> dates = parseDates(text);
        return dates.isEmpty() ? null : dates.get(0);
    }
}
