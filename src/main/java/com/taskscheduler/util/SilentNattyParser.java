package com.taskscheduler.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * A wrapper for Natty's Parser that completely silences all its output
 * by redirecting System.out and System.err during parse operations.
 */
public class SilentNattyParser {
    private final Parser parser;
    
    static {
        // Static initialization to disable all Natty logging
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        
        try {
            // Force SLF4J to initialize with our settings
            org.slf4j.LoggerFactory.getILoggerFactory();
        } catch (Exception e) {
            // Ignore any errors during logging setup
        }
    }
    
    public SilentNattyParser() {
        this.parser = new Parser();
    }
    
    /**
     * Parse text for dates while redirecting all output to prevent console logging.
     * 
     * @param text The text to parse for date information
     * @return A list of DateGroups found in the text
     */
    public List<DateGroup> parse(String text) {
        // Save the original output and error streams
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        try {
            // Redirect output to throw it away
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            
            // Perform the actual parsing
            return parser.parse(text);
        } finally {
            // Restore the original output streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
    
    /**
     * Parse text for dates and return a flattened list of Date objects.
     * 
     * @param text The text to parse
     * @return A list of Date objects extracted from the text
     */
    public List<Date> parseDates(String text) {
        List<DateGroup> groups = parse(text);
        List<Date> dates = new java.util.ArrayList<>();
        
        for (DateGroup group : groups) {
            dates.addAll(group.getDates());
        }
        
        return dates;
    }
}
