package com.taskscheduler.nlp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

/**
 * Utility class for detecting command intents from natural language input.
 */
public class IntentDetector {
    private static final Logger logger = Logger.getLogger(IntentDetector.class.getName());
    private final Tokenizer tokenizer;
    
    // Intent types
    public static final String INTENT_ADD = "add";
    public static final String INTENT_LIST = "list";
    public static final String INTENT_COMPLETE = "complete";
    public static final String INTENT_DELETE = "delete";
    public static final String INTENT_HELP = "help";
    public static final String INTENT_UNKNOWN = "unknown";
    
    // Keywords associated with each intent
    private final Map<String, List<String>> intentKeywords;
    
    public IntentDetector() {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.intentKeywords = new HashMap<>();
        
        // Initialize intent keywords
        intentKeywords.put(INTENT_ADD, Arrays.asList(
            "add", "create", "schedule", "remind", "set", "new", "make", "task", "remember", "appointment"
        ));
        
        intentKeywords.put(INTENT_LIST, Arrays.asList(
            "list", "show", "display", "view", "get", "find", "search", "what", "upcoming"
        ));
        
        intentKeywords.put(INTENT_COMPLETE, Arrays.asList(
            "complete", "done", "finish", "mark", "completed", "finished", "check"
        ));
        
        intentKeywords.put(INTENT_DELETE, Arrays.asList(
            "delete", "remove", "cancel", "clear"
        ));
        
        intentKeywords.put(INTENT_HELP, Arrays.asList(
            "help", "how", "guide", "manual", "instructions"
        ));
    }
    
    /**
     * Detects the command intent from a natural language input.
     * 
     * @param input The natural language input
     * @return The detected intent (one of the INTENT_* constants)
     */
    public String detectIntent(String input) {
        if (input == null || input.trim().isEmpty()) {
            return INTENT_UNKNOWN;
        }
        
        // Lowercase and tokenize the input
        String normalizedInput = input.toLowerCase().trim();
        String[] tokens = tokenizer.tokenize(normalizedInput);
        
        // Check for explicit commands first (highest confidence)
        if (normalizedInput.startsWith("add ") || 
            normalizedInput.startsWith("create ") ||
            normalizedInput.startsWith("remind ")) {
            return INTENT_ADD;
        }
        
        if (normalizedInput.equals("list") || 
            normalizedInput.startsWith("list ") || 
            normalizedInput.startsWith("show ")) {
            return INTENT_LIST;
        }
        
        if (normalizedInput.startsWith("complete ") || 
            normalizedInput.startsWith("done ")) {
            return INTENT_COMPLETE;
        }
        
        if (normalizedInput.startsWith("delete ") || 
            normalizedInput.startsWith("remove ")) {
            return INTENT_DELETE;
        }
        
        if (normalizedInput.equals("help")) {
            return INTENT_HELP;
        }
        
        // For less explicit inputs, score each intent based on keyword matches
        Map<String, Integer> intentScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
            String intent = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int score = 0;
            for (String token : tokens) {
                if (keywords.contains(token)) {
                    score++;
                }
            }
            
            intentScores.put(intent, score);
        }
          // Find the intent with the highest score
        String bestIntent = INTENT_UNKNOWN;
        int highestScore = 0;
        
        for (Map.Entry<String, Integer> entry : intentScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestIntent = entry.getKey();
            }
        }
        
        // If we have a high enough confidence, return the detected intent
        if (highestScore > 0) {
            final String finalIntent = bestIntent;
            final int finalScore = highestScore;
            logger.fine(() -> "Detected intent: " + finalIntent + " with confidence score: " + finalScore);
            return bestIntent;
        }
        
        // If no clear intent is detected, try to infer from sentence structure
        if (containsTimeExpression(normalizedInput) && 
            !normalizedInput.contains("list") && 
            !normalizedInput.contains("show")) {
            // Sentences with time expressions are likely to be task creation
            return INTENT_ADD;
        }
        
        return INTENT_UNKNOWN;
    }
    
    /**
     * Checks if the input contains a time expression (simple heuristic approach).
     */
    private boolean containsTimeExpression(String input) {
        // Check for common time-related words
        String[] timeKeywords = {"today", "tomorrow", "next", "on", "at", "by", "before", "after", "pm", "am"};
        
        for (String keyword : timeKeywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        
        // Check for days of week
        String[] daysOfWeek = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : daysOfWeek) {
            if (input.contains(day)) {
                return true;
            }
        }
        
        // Check for date patterns (simple regex patterns)
        String datePattern = "\\d{1,2}[/-]\\d{1,2}"; // Matches patterns like 6/4, 06/04, etc.
        return Pattern.compile(datePattern).matcher(input).find();
    }
    
    /**
     * Extracts the main task description from natural language input after intent is detected.
     * 
     * @param input The natural language input
     * @param intent The detected intent
     * @return The task description
     */
    public String extractTaskDescription(String input, String intent) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        String normalizedInput = input.trim();
        
        // For explicit command patterns, extract the description after the command word
        if (intent.equals(INTENT_ADD)) {
            if (normalizedInput.startsWith("add ")) {
                return normalizedInput.substring(4).trim();
            }
            if (normalizedInput.startsWith("create ")) {
                return normalizedInput.substring(7).trim();
            }
            if (normalizedInput.startsWith("remind ")) {
                String desc = normalizedInput.substring(7).trim();
                // Remove "me to" or "me about" common patterns
                if (desc.startsWith("me to ")) {
                    return desc.substring(6).trim();
                }
                if (desc.startsWith("me about ")) {
                    return desc.substring(9).trim();
                }
                if (desc.startsWith("me ")) {
                    return desc.substring(3).trim();
                }
                return desc;
            }
        }
        
        // For non-explicit commands, we need to extract the task description
        // by removing temporal expressions
        if (intent.equals(INTENT_ADD)) {
            // This is simplified - for a complete solution, you'd need to
            // use OpenNLP's entity extraction to more accurately identify
            // and remove date/time phrases
            
            // Simplified approach: common time marker words
            String[] timeMarkers = {"today", "tomorrow", "next week", "next month",
                                    "on monday", "on tuesday", "on wednesday",
                                    "on thursday", "on friday", "on saturday", "on sunday"};
            
            String description = normalizedInput;
            
            for (String marker : timeMarkers) {
                int idx = description.toLowerCase().indexOf(marker);
                if (idx > 0) {
                    // Keep only the part before the time marker
                    description = description.substring(0, idx).trim();
                }
            }
            
            return description;
        }
        
        return normalizedInput;
    }
}
