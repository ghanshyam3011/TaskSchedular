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
    public static final String INTENT_CLEAR = "clear";
    public static final String INTENT_UNKNOWN = "unknown";
    
    // Keywords for email notifications
    private static final String[] EMAIL_KEYWORDS = {
        "email", "mail", "notify", "notification", "remind", "reminder", "alert", "message"
    };
    
    // Keywords associated with each intent
    private final Map<String, List<String>> intentKeywords;
    
    public IntentDetector() {
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.intentKeywords = new HashMap<>();
          // Initialize intent keywords
        intentKeywords.put(INTENT_ADD, Arrays.asList(
            "add", "create", "schedule", "remind", "set", "new", "make", "task", "remember", "appointment",
            "book", "plan", "do", "work", "meeting", "call", "email", "urgent", "critical", "important"
        ));
        
        intentKeywords.put(INTENT_LIST, Arrays.asList(
            "list", "show", "display", "view", "get", "find", "search", "what", "upcoming", "tasks", "todo"
        ));
        
        intentKeywords.put(INTENT_COMPLETE, Arrays.asList(
            "complete", "done", "finish", "mark", "completed", "finished", "check", "tick"
        ));
          intentKeywords.put(INTENT_DELETE, Arrays.asList(
            "delete", "remove", "cancel", "drop"
        ));
        
        intentKeywords.put(INTENT_CLEAR, Arrays.asList(
            "clear", "refresh", "clean", "cls", "reset", "wipe"
        ));        
        
        intentKeywords.put(INTENT_HELP, Arrays.asList(
            "help", "how", "guide", "manual", "instructions", "usage"
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
        
        if (normalizedInput.equals("clear") ||
            normalizedInput.equals("refresh") ||
            normalizedInput.equals("cls") ||
            normalizedInput.contains("clear screen") ||
            normalizedInput.contains("refresh screen")) {
            return INTENT_CLEAR;
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
            
            // Boost score for task creation if priority keywords are detected
            if (intent.equals(INTENT_ADD) && containsPriorityKeywords(normalizedInput)) {
                score += 2; // Strong boost for priority-related inputs
            }
            
            // Boost score for task creation if time expressions are present
            if (intent.equals(INTENT_ADD) && containsTimeExpression(normalizedInput)) {
                score += 1; // Moderate boost for time-related inputs
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
            String rawDescription;
            
            if (normalizedInput.startsWith("add ")) {
                rawDescription = normalizedInput.substring(4).trim();
            }
            else if (normalizedInput.startsWith("create ")) {
                rawDescription = normalizedInput.substring(7).trim();
            }            else if (normalizedInput.startsWith("remind ")) {
                String desc = normalizedInput.substring(7).trim();
                // More conservative processing - only remove "me to" if it's at the very beginning
                if (desc.startsWith("me to ")) {
                    rawDescription = desc.substring(6).trim();
                    
                    // Remove email phrases from the task description
                    for (String emailKeyword : EMAIL_KEYWORDS) {
                        if (rawDescription.contains(emailKeyword + " me")) {
                            rawDescription = rawDescription.replaceAll("(?i)\\b" + emailKeyword + "\\s+me\\b", "").trim();
                        }
                    }
                } else if (desc.startsWith("me ")) {
                    // Check if this is "me and ..." pattern, if so, include everything
                    if (desc.startsWith("me and ")) {
                        rawDescription = desc; // Keep the full description including "me and"
                    } else {
                        rawDescription = desc.substring(3).trim();
                    }
                } else {
                    rawDescription = desc;
                }
            }
            else {
                rawDescription = normalizedInput;
            }
            
            // Apply time expression cleanup to the extracted description
            return cleanupTimeExpressions(rawDescription);
        }
        
        // For non-explicit commands, we need to extract the task description
        // by removing temporal expressions
        if (intent.equals(INTENT_ADD)) {
            // This is simplified - for a complete solution, you'd need to
            // use OpenNLP's entity extraction to more accurately identify
            // and remove date/time phrases
              // Enhanced approach: remove time expressions while preserving priority context
            String[] timeMarkers = {"today after", "today in", "tomorrow after", "tomorrow in",
                                    "today at", "tomorrow at", "next week", "next month",
                                    "on monday", "on tuesday", "on wednesday",
                                    "on thursday", "on friday", "on saturday", "on sunday"};
            
            String description = normalizedInput;
            
            for (String marker : timeMarkers) {
                int idx = description.toLowerCase().indexOf(marker);
                if (idx >= 0) {
                    String beforeTime = description.substring(0, idx).trim();
                    String afterTime = description.substring(idx + marker.length()).trim();
                    
                    // If there's priority context after the time marker, preserve it
                    if (containsPriorityKeywords(afterTime)) {
                        // Extract priority-related words from the after part
                        String[] words = afterTime.split("\\s+");
                        StringBuilder priorityContext = new StringBuilder();
                        for (String word : words) {
                            if (word.toLowerCase().matches("(urgent|critical|important|asap|priority|rush|fast|quickly|pressing|vital|essential).*")) {
                                priorityContext.append(" ").append(word);
                            }
                        }
                        description = beforeTime + priorityContext.toString();
                    } else {
                        description = beforeTime;
                    }
                    break;
                }
            }
            
            // Also handle relative time expressions like "after X minutes", "in Y hours"
            java.util.regex.Pattern[] relativeTimePatterns = {
                java.util.regex.Pattern.compile("\\bafter\\s+\\d+\\s+minutes?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("\\bin\\s+\\d+\\s+minutes?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("\\bafter\\s+\\d+\\s+hours?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
                java.util.regex.Pattern.compile("\\bin\\s+\\d+\\s+hours?\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
            };
            
            for (java.util.regex.Pattern pattern : relativeTimePatterns) {
                java.util.regex.Matcher matcher = pattern.matcher(description);
                if (matcher.find()) {
                    String beforeTime = description.substring(0, matcher.start()).trim();
                    String afterTime = description.substring(matcher.end()).trim();
                    
                    // Preserve priority context from the after part
                    if (containsPriorityKeywords(afterTime)) {
                        String[] words = afterTime.split("\\s+");
                        StringBuilder priorityContext = new StringBuilder();
                        for (String word : words) {
                            if (word.toLowerCase().matches("(urgent|critical|important|asap|priority|rush|fast|quickly|pressing|vital|essential).*")) {
                                priorityContext.append(" ").append(word);
                            }
                        }
                        description = beforeTime + priorityContext.toString();
                    } else {
                        description = beforeTime;
                    }
                    break;
                }
            }
            
            // Clean up extra spaces
            description = description.replaceAll("\\s+", " ").trim();
            
            return description;
        }
          return normalizedInput;
    }
    
    /**
     * Checks if the input contains priority-related keywords.
     * 
     * @param input The input text to check
     * @return true if priority keywords are found
     */
    private boolean containsPriorityKeywords(String input) {
        if (input == null) return false;
        
        String normalized = input.toLowerCase();
        
        // Check for explicit priority keywords
        String[] priorityKeywords = {
            "urgent", "critical", "important", "asap", "immediately", "emergency",
            "high priority", "low priority", "rush", "quickly", "fast", 
            "time sensitive", "pressing", "vital", "essential", "priority",
            "soon", "deadline", "later", "someday", "must do", "top priority",
            "urgent work", "urgent task", "urgent meeting", "urgent call",
            "critical work", "critical task", "critical meeting", "important work",
            "important task", "important meeting"
        };
        
        for (String keyword : priorityKeywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the input contains email notification intent.
     * 
     * @param input The natural language input
     * @return true if email notification is requested, false otherwise
     */
    public boolean hasEmailIntent(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String normalizedInput = input.toLowerCase().trim();
        
        // Check for direct email indicator words
        for (String keyword : EMAIL_KEYWORDS) {
            // Check if the input contains any of the email keywords
            if (normalizedInput.contains(keyword)) {
                return true;
            }
        }
        
        // Check for common phrases indicating email notification
        String[] emailPhrases = {
            "notify me", "send me", "remind me", "email me", "send an email",
            "get an email", "receive an email", "send notification", "by email",
            "with notification", "with email notification", "and email me"
        };
        
        for (String phrase : emailPhrases) {
            if (normalizedInput.contains(phrase)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Strips email-related phrases from the input text.
     * 
     * @param input The input text
     * @return The text with email phrases removed
     */
    public String stripEmailPhrases(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String result = input;
        
        // Remove common email notification phrases
        String[] phrasesToRemove = {
            "and email me", "and notify me", "and send me an email", 
            "also email me", "also notify me", "email me", "notify me by email",
            "with email notification", "with notification", "and remind me by email",
            "send me a reminder", "send notification", "notify me", "remind me"
        };
        
        for (String phrase : phrasesToRemove) {
            result = result.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(phrase) + "\\b", "");
        }
        
        // Clean up extra spaces
        result = result.replaceAll("\\s+", " ").trim();
          return result;
    }
    
    /**
     * Cleans up time expressions from a task description while preserving priority context.
     * 
     * @param description The task description to clean
     * @return The cleaned description
     */
    private String cleanupTimeExpressions(String description) {
        if (description == null || description.trim().isEmpty()) {
            return description;
        }
        
        String result = description.trim();
        
        // Enhanced approach: remove time expressions while preserving priority context
        String[] timeMarkers = {"today after", "today in", "tomorrow after", "tomorrow in",
                                "today at", "tomorrow at", "next week", "next month",
                                "on monday", "on tuesday", "on wednesday",
                                "on thursday", "on friday", "on saturday", "on sunday"};
        
        for (String marker : timeMarkers) {
            int idx = result.toLowerCase().indexOf(marker);
            if (idx >= 0) {
                String beforeTime = result.substring(0, idx).trim();
                String afterTime = result.substring(idx + marker.length()).trim();
                
                // If there's priority context after the time marker, preserve it
                if (containsPriorityKeywords(afterTime)) {
                    // Extract priority-related words from the after part
                    String[] words = afterTime.split("\\s+");
                    StringBuilder priorityContext = new StringBuilder();
                    for (String word : words) {
                        if (word.toLowerCase().matches("(urgent|critical|important|asap|priority|rush|fast|quickly|pressing|vital|essential).*")) {
                            priorityContext.append(" ").append(word);
                        }
                    }
                    result = beforeTime + priorityContext.toString();
                } else {
                    result = beforeTime;
                }
                break;
            }
        }
        
        // Also handle relative time expressions like "after X minutes", "in Y hours"
        java.util.regex.Pattern[] relativeTimePatterns = {
            java.util.regex.Pattern.compile("\\bafter\\s+\\d+\\s+minutes?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
            java.util.regex.Pattern.compile("\\bin\\s+\\d+\\s+minutes?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
            java.util.regex.Pattern.compile("\\bafter\\s+\\d+\\s+hours?\\b", java.util.regex.Pattern.CASE_INSENSITIVE),
            java.util.regex.Pattern.compile("\\bin\\s+\\d+\\s+hours?\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
        };
        
        for (java.util.regex.Pattern pattern : relativeTimePatterns) {
            java.util.regex.Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                String beforeTime = result.substring(0, matcher.start()).trim();
                String afterTime = result.substring(matcher.end()).trim();
                
                // Preserve priority context from the after part
                if (containsPriorityKeywords(afterTime)) {
                    String[] words = afterTime.split("\\s+");
                    StringBuilder priorityContext = new StringBuilder();
                    for (String word : words) {
                        if (word.toLowerCase().matches("(urgent|critical|important|asap|priority|rush|fast|quickly|pressing|vital|essential).*")) {
                            priorityContext.append(" ").append(word);
                        }
                    }
                    result = beforeTime + priorityContext.toString();
                } else {
                    result = beforeTime;
                }
                break;
            }
        }
        
        // Clean up extra spaces
        result = result.replaceAll("\\s+", " ").trim();
        
        return result;
    }
}
