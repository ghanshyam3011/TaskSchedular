package com.taskscheduler.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for creating beautiful ASCII tables
 */
public class Table {
    private List<String> headers;
    private List<List<String>> rows;
    private List<Integer> columnWidths;
    private String borderColor;
    private String headerColor;
    private String dataColor;
    
    public Table() {
        this.headers = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columnWidths = new ArrayList<>();
        this.borderColor = Colors.CYAN;
        this.headerColor = Colors.BLUE_BOLD;
        this.dataColor = Colors.WHITE;
    }
    
    public Table setHeaders(String... headers) {
        this.headers = Arrays.asList(headers);
        // Initialize column widths based on header lengths
        columnWidths.clear();
        for (String header : headers) {
            columnWidths.add(header.length());
        }
        return this;
    }
    
    public Table addRow(String... rowData) {
        List<String> row = Arrays.asList(rowData);
        rows.add(row);
        
        // Update column widths if necessary
        for (int i = 0; i < row.size() && i < columnWidths.size(); i++) {
            int currentWidth = stripAnsiCodes(row.get(i)).length();
            if (currentWidth > columnWidths.get(i)) {
                columnWidths.set(i, currentWidth);
            }
        }
        return this;
    }
    
    public Table setBorderColor(String color) {
        this.borderColor = color;
        return this;
    }
    
    public Table setHeaderColor(String color) {
        this.headerColor = color;
        return this;
    }
    
    public Table setDataColor(String color) {
        this.dataColor = color;
        return this;
    }
    
    public String render() {
        if (headers.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Top border
        sb.append(borderColor).append(Icons.TOP_LEFT);
        for (int i = 0; i < headers.size(); i++) {
            sb.append(repeat(Icons.HORIZONTAL, columnWidths.get(i) + 2));
            if (i < headers.size() - 1) {
                sb.append(Icons.T_DOWN);
            }
        }
        sb.append(Icons.TOP_RIGHT).append(Colors.RESET).append("\n");
        
        // Headers
        sb.append(borderColor).append(Icons.VERTICAL).append(Colors.RESET);
        for (int i = 0; i < headers.size(); i++) {
            sb.append(" ").append(headerColor).append(padRight(headers.get(i), columnWidths.get(i)))
              .append(Colors.RESET).append(" ");
            sb.append(borderColor).append(Icons.VERTICAL).append(Colors.RESET);
        }
        sb.append("\n");
        
        // Header separator
        sb.append(borderColor).append(Icons.T_RIGHT);
        for (int i = 0; i < headers.size(); i++) {
            sb.append(repeat(Icons.HORIZONTAL, columnWidths.get(i) + 2));
            if (i < headers.size() - 1) {
                sb.append(Icons.CROSS);
            }
        }
        sb.append(Icons.T_LEFT).append(Colors.RESET).append("\n");
        
        // Data rows
        for (List<String> row : rows) {
            sb.append(borderColor).append(Icons.VERTICAL).append(Colors.RESET);
            for (int i = 0; i < headers.size(); i++) {
                String cellData = i < row.size() ? row.get(i) : "";
                sb.append(" ").append(dataColor).append(padRight(cellData, columnWidths.get(i)))
                  .append(Colors.RESET).append(" ");
                sb.append(borderColor).append(Icons.VERTICAL).append(Colors.RESET);
            }
            sb.append("\n");
        }
        
        // Bottom border
        sb.append(borderColor).append(Icons.BOTTOM_LEFT);
        for (int i = 0; i < headers.size(); i++) {
            sb.append(repeat(Icons.HORIZONTAL, columnWidths.get(i) + 2));
            if (i < headers.size() - 1) {
                sb.append(Icons.T_UP);
            }
        }
        sb.append(Icons.BOTTOM_RIGHT).append(Colors.RESET);
        
        return sb.toString();
    }
    
    private String padRight(String text, int width) {
        int actualLength = stripAnsiCodes(text).length();
        int padding = Math.max(0, width - actualLength);
        return text + repeat(" ", padding);
    }
    
    private String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private String stripAnsiCodes(String text) {
        return text.replaceAll("\033\\[[0-9;]*m", "");
    }
}
