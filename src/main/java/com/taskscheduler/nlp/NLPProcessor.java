package com.taskscheduler.nlp;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Main class for processing natural language inputs for the task scheduler.
 */
public class NLPProcessor {
    private static final Logger logger = Logger.getLogger(NLPProcessor.class.getName());
    
    private final DateTimeParser dateTimeParser;
    private final IntentDetector intentDetector;
    
    public NLPProcessor() {
        this.dateTimeParser = new DateTimeParser();
        this.intentDetector = new IntentDetector();
    }
    
    /**
     * Process a natural language input and convert it to a structured command.
     * 
     * @param input The natural language input from the user
     * @return A processed command that can be executed by the system, or null if not recognized
     */    public ProcessedCommand processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        // Skip NLP processing for system commands
        if (input.trim().startsWith("email-notification ")) {
            logger.info(() -> "Skipping NLP processing for system command: " + input);
            return null;
        }
        
        String normalizedInput = input.trim();
        String intent = intentDetector.detectIntent(normalizedInput);
          // If we couldn't detect an intent, return null
        if (intent.equals(IntentDetector.INTENT_UNKNOWN)) {
            logger.info(() -> "Could not detect intent from input: " + normalizedInput);
            return null;
        }
        
        logger.info(() -> "Detected intent: " + intent + " from input: " + normalizedInput);
        
        // Process the input based on the intent
        switch (intent) {
            case IntentDetector.INTENT_ADD:
                return processAddTaskIntent(normalizedInput);
            case IntentDetector.INTENT_LIST:
                return processListTasksIntent(normalizedInput);
            case IntentDetector.INTENT_COMPLETE:
                return processCompleteTaskIntent(normalizedInput);
            case IntentDetector.INTENT_DELETE:
                return processDeleteTaskIntent(normalizedInput);
            case IntentDetector.INTENT_HELP:
                return new ProcessedCommand(intent, "help");
            default:
                return null;
        }
    }    /**
     * Process an input with an "add task" intent.
     */    private ProcessedCommand processAddTaskIntent(String input) {
        // First, check if there's an email intent
        boolean hasEmailIntent = intentDetector.hasEmailIntent(input);
        
        // Detect priority from the input with enhanced context awareness
        com.taskscheduler.Priority detectedPriority = com.taskscheduler.Priority.detectFromText(input);
        
        // Extract the clean task description (without time expressions or email phrases)
        String taskDescription = intentDetector.extractTaskDescription(input, IntentDetector.INTENT_ADD);
        logger.fine(() -> "Extracted task description: " + taskDescription);
        
        // If no specific priority detected but task description contains priority context, re-check
        if (detectedPriority == com.taskscheduler.Priority.MEDIUM && taskDescription != null) {
            com.taskscheduler.Priority contextPriority = com.taskscheduler.Priority.detectFromText(taskDescription);
            if (contextPriority != com.taskscheduler.Priority.MEDIUM) {
                detectedPriority = contextPriority;
                final com.taskscheduler.Priority finalPriority = detectedPriority;
                logger.info(() -> "Detected priority from task description context: " + finalPriority.getDisplayName());
            }
        }
          // Strip email-related phrases from input before parsing dates
        final String cleanInput;
        if (hasEmailIntent) {
            cleanInput = intentDetector.stripEmailPhrases(input);
            logger.info(() -> "Stripped email phrases for date parsing: " + cleanInput);
        } else {
            cleanInput = input;
        }
          // Look for a date/time in the cleaned input to avoid parsing errors
        LocalDateTime dateTime = dateTimeParser.extractFirstDateTime(cleanInput);
        
        // The CommandHandler expects: add "Task Title" at HH:mm for today's tasks
        // or add "Task Title" due yyyy-MM-dd HH:mm for future tasks
        StringBuilder command = new StringBuilder();
        command.append("add \"").append(taskDescription).append("\"");
          if (dateTime != null) {
            LocalDateTime now = LocalDateTime.now();
            boolean isToday = dateTime.toLocalDate().equals(now.toLocalDate());
            
            if (isToday) {
                // If it's today, use the "at HH:mm" format
                command.append(" at ").append(dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            } else {
                // For future dates, use the "due yyyy-MM-dd HH:mm" format
                command.append(" due ").append(dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        } else {
            // If no date/time found, default to tomorrow at noon
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
            command.append(" due ").append(tomorrow.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }        
          // Add priority if detected
        if (detectedPriority != com.taskscheduler.Priority.MEDIUM) {
            command.append(" --priority ").append(detectedPriority.name().toLowerCase());
            final com.taskscheduler.Priority finalDetectedPriority = detectedPriority;
            logger.info(() -> "Detected priority: " + finalDetectedPriority.getDisplayName());
        }
        
        // Use the previously determined email intent
        if (hasEmailIntent) {
            // Add email notification flag to the command
            command.append(" --notify-email");
            logger.info(() -> "Added email notification flag based on natural language request");
        }
        
        return new ProcessedCommand(IntentDetector.INTENT_ADD, command.toString());
    }
    
    /**
     * Process an input with a "list tasks" intent.
     */
    private ProcessedCommand processListTasksIntent(String input) {
        String normalizedInput = input.toLowerCase();
        
        if (normalizedInput.contains("upcoming")) {
            return new ProcessedCommand(IntentDetector.INTENT_LIST, "list upcoming");
        } else if (normalizedInput.contains("overdue")) {
            return new ProcessedCommand(IntentDetector.INTENT_LIST, "list overdue");
        } else if (normalizedInput.contains("today")) {
            return new ProcessedCommand(IntentDetector.INTENT_LIST, "list today");
        } else {
            return new ProcessedCommand(IntentDetector.INTENT_LIST, "list");
        }
    }
    
    /**
     * Process an input with a "complete task" intent.
     */
    private ProcessedCommand processCompleteTaskIntent(String input) {
        // Try to extract a task number
        String[] tokens = input.split("\\s+");
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                return new ProcessedCommand(IntentDetector.INTENT_COMPLETE, "complete " + token);
            }
        }
        
        // If no task number found, return a generic command
        return new ProcessedCommand(IntentDetector.INTENT_COMPLETE, "complete ");
    }
    
    /**
     * Process an input with a "delete task" intent.
     */
    private ProcessedCommand processDeleteTaskIntent(String input) {
        // Try to extract a task number
        String[] tokens = input.split("\\s+");
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                return new ProcessedCommand(IntentDetector.INTENT_DELETE, "delete " + token);
            }
        }
        
        // If no task number found, return a generic command
        return new ProcessedCommand(IntentDetector.INTENT_DELETE, "delete ");
    }
    
    /**
     * Class representing a processed command with intent and formatted command string.
     */
    public static class ProcessedCommand {
        private final String intent;
        private final String formattedCommand;
        
        public ProcessedCommand(String intent, String formattedCommand) {
            this.intent = intent;
            this.formattedCommand = formattedCommand;
        }
        
        public String getIntent() {
            return intent;
        }
        
        public String getFormattedCommand() {
            return formattedCommand;
        }
        
        @Override
        public String toString() {            return "ProcessedCommand{intent='" + intent + "', command='" + formattedCommand + "'}";
        }
    }
}
