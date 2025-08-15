package com.taskscheduler.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class SilentNattyParser {
    private final Parser parser;
    
    static {
        try {
            org.slf4j.LoggerFactory.getILoggerFactory();
        } catch (Exception e) {
            // silent fail
        }
    }
    
    public SilentNattyParser() {
        this.parser = new Parser();
    }
    
    public List<DateGroup> parse(String text) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            
            return parser.parse(text);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
    
    public List<Date> parseDates(String text) {
        List<DateGroup> groups = parse(text);
        List<Date> dates = new java.util.ArrayList<>();
        
        for (DateGroup group : groups) {
            dates.addAll(group.getDates());
        }
        
        return dates;
    }
}
