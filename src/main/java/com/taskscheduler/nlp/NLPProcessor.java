package com.taskscheduler.nlp;

import java.time.LocalDateTime;
import java.util.logging.Logger;

public class NLPProcessor {
    private static final Logger logger = Logger.getLogger(NLPProcessor.class.getName());
    
    private final DateTimeParser dateTimeParser;
    private final IntentDetector intentDetector;
    
    public NLPProcessor() {
        this.dateTimeParser = new DateTimeParser();
        this.intentDetector = new IntentDetector();
    }
    
    public ProcessedCommand processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        if (input.trim().startsWith("email-notification ")) {
            logger.info(() -> "Skipping NLP processing for system command: " + input);
            return null;
        }
        
        String normalizedInput = input.trim();
        String intent = intentDetector.detectIntent(normalizedInput);
        
        if (intent.equals(IntentDetector.INTENT_UNKNOWN)) {
            logger.info(() -> "Could not detect intent from input: " + normalizedInput);
            return null;
        }
        
        logger.info(() -> "Detected intent: " + intent + " from input: " + normalizedInput);
        
        switch (intent) {
            case IntentDetector.INTENT_ADD:
                return processAddTaskIntent(normalizedInput);
            case IntentDetector.INTENT_LIST:
                return processListTasksIntent(normalizedInput);            
            case IntentDetector.INTENT_COMPLETE:
                return processCompleteTaskIntent(normalizedInput);
            case IntentDetector.INTENT_DELETE:
                return processDeleteTaskIntent(normalizedInput);
            case IntentDetector.INTENT_CLEAR:
                return new ProcessedCommand(intent, "clear");
            case IntentDetector.INTENT_HELP:
                return new ProcessedCommand(intent, "help");
            default:
                return null;
        }
    }
    
    private ProcessedCommand processAddTaskIntent(String input) {
        boolean hasEmailIntent = intentDetector.hasEmailIntent(input);
        com.taskscheduler.Priority detectedPriority = com.taskscheduler.Priority.detectFromText(input);
        
        String taskDescription = intentDetector.extractTaskDescription(input, IntentDetector.INTENT_ADD);
        logger.fine(() -> "Extracted task description: " + taskDescription);
        
        if (detectedPriority == com.taskscheduler.Priority.MEDIUM && taskDescription != null) {
            com.taskscheduler.Priority contextPriority = com.taskscheduler.Priority.detectFromText(taskDescription);
            if (contextPriority != com.taskscheduler.Priority.MEDIUM) {
                detectedPriority = contextPriority;
                final com.taskscheduler.Priority finalPriority = detectedPriority;
                logger.info(() -> "Detected priority from task description context: " + finalPriority.getDisplayName());
            }
        }
        
        final String cleanInput;
        if (hasEmailIntent) {
            cleanInput = intentDetector.stripEmailPhrases(input);
            logger.info(() -> "Stripped email phrases for date parsing: " + cleanInput);
        } else {
            cleanInput = input;
        }
        
        LocalDateTime dateTime = dateTimeParser.extractFirstDateTime(cleanInput);
        
        StringBuilder command = new StringBuilder();
        command.append("add \"").append(taskDescription).append("\"");
        
        if (dateTime != null) {
            LocalDateTime now = LocalDateTime.now();
            boolean isToday = dateTime.toLocalDate().equals(now.toLocalDate());
            
            if (isToday) {
                command.append(" at ").append(dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
            } else {
                command.append(" due ").append(dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        } else {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
            command.append(" due ").append(tomorrow.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        
        if (detectedPriority != com.taskscheduler.Priority.MEDIUM) {
            command.append(" --priority ").append(detectedPriority.name().toLowerCase());
            final com.taskscheduler.Priority finalDetectedPriority = detectedPriority;
            logger.info(() -> "Detected priority: " + finalDetectedPriority.getDisplayName());
        }
        
        String[] inputWords = input.split("\\s+");
        for (String word : inputWords) {
            if (word.startsWith("--") && word.length() > 2) {
                String potentialTag = word.substring(2);
                if (!isKnownParameter(potentialTag)) {
                    command.append(" --tag ").append(potentialTag);
                    logger.info(() -> "Detected tag from input: " + potentialTag);
                }
            }
        }
        
        if (hasEmailIntent) {
            if (!command.toString().contains("--notify-email")) {
                command.append(" --notify-email");
                logger.info(() -> "Added email notification flag based on natural language request");
            }
        } else {
            if (command.toString().contains("--notify-email")) {
                logger.info(() -> "Removing incorrectly added email notification flag");
                String cmdStr = command.toString().replace("--notify-email", "").trim();
                command.setLength(0);
                command.append(cmdStr);
            }
        }
        
        return new ProcessedCommand(IntentDetector.INTENT_ADD, command.toString());
    }
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
    
    private ProcessedCommand processCompleteTaskIntent(String input) {
        String[] tokens = input.split("\\s+");
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                return new ProcessedCommand(IntentDetector.INTENT_COMPLETE, "complete " + token);
            }
        }
        
        return new ProcessedCommand(IntentDetector.INTENT_COMPLETE, "complete ");
    }
    
    private ProcessedCommand processDeleteTaskIntent(String input) {
        String[] tokens = input.split("\\s+");
        for (String token : tokens) {
            if (token.matches("\\d+")) {
                return new ProcessedCommand(IntentDetector.INTENT_DELETE, "delete " + token);
            }
        }
        
        return new ProcessedCommand(IntentDetector.INTENT_DELETE, "delete ");
    }
    
    private boolean isKnownParameter(String param) {
        String[] knownParams = {
            "notify-email", "repeat", "end", "reminder", "email", "priority", "tag"
        };
        
        for (String known : knownParams) {
            if (param.equals(known)) {
                return true;
            }
        }
        return false;
    }

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
        
        public String getIntentType() {
            return intent;
        }
        
        public String getFormattedCommand() {
            return formattedCommand;
        }
        
        public String getCommand() {
            return formattedCommand;
        }
        
        @Override
        public String toString() {
            return "ProcessedCommand{intent='" + intent + "', command='" + formattedCommand + "'}";
        }
    }
}
