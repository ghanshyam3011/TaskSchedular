package com.taskscheduler;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.time.Duration;

public class CommandPatternAnalyzer {
    private final CommandLogger commandLogger;
    private LocalDateTime lastSuggestionTime = null;
    private static final Duration SUGGESTION_COOLDOWN = Duration.ofMinutes(30);

    public CommandPatternAnalyzer(CommandLogger commandLogger) {
        this.commandLogger = commandLogger;
    }

    public Map<String, Integer> getCommandFrequency() {
        List<CommandLogger.CommandEntry> history = commandLogger.getCommandHistory();
        return history.stream()
            .collect(Collectors.groupingBy(
                CommandLogger.CommandEntry::getCommand,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    public Map<Integer, Integer> getHourlyDistribution(String command) {
        List<CommandLogger.CommandEntry> history = commandLogger.getCommandHistory();
        return history.stream()
            .filter(entry -> entry.getCommand().equals(command))
            .collect(Collectors.groupingBy(
                entry -> entry.getTimestamp().getHour(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    public Map<DayOfWeek, Integer> getDailyDistribution(String command) {
        List<CommandLogger.CommandEntry> history = commandLogger.getCommandHistory();
        return history.stream()
            .filter(entry -> entry.getCommand().equals(command))
            .collect(Collectors.groupingBy(
                entry -> entry.getTimestamp().getDayOfWeek(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    public List<String> getMostFrequentCommands(int limit) {
        Map<String, Integer> frequency = getCommandFrequency();
        return frequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public int getMostCommonHour(String command) {
        Map<Integer, Integer> hourlyDist = getHourlyDistribution(command);
        return hourlyDist.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(-1);
    }

    public DayOfWeek getMostCommonDay(String command) {
        Map<DayOfWeek, Integer> dailyDist = getDailyDistribution(command);
        return dailyDist.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public boolean isCommandFrequentAtTime(String command, LocalDateTime time) {
        int mostCommonHour = getMostCommonHour(command);
        DayOfWeek mostCommonDay = getMostCommonDay(command);
        
        if (mostCommonHour == -1 || mostCommonDay == null) {
            return false;
        }

        // Check if the current time is within 30 minutes of the most common hour
        int currentHour = time.getHour();
        int currentMinute = time.getMinute();
        boolean isNearCommonHour = (currentHour == mostCommonHour && currentMinute <= 30) ||
                                 (currentHour == (mostCommonHour + 1) % 24 && currentMinute > 30);

        return isNearCommonHour && time.getDayOfWeek() == mostCommonDay;
    }

    public String getCommandSuggestion(LocalDateTime currentTime) {
        // Check if we're in cooldown period
        if (lastSuggestionTime != null && 
            Duration.between(lastSuggestionTime, currentTime).compareTo(SUGGESTION_COOLDOWN) < 0) {
            return null;
        }

        List<String> frequentCommands = getMostFrequentCommands(5);
        
        for (String command : frequentCommands) {
            if (isCommandFrequentAtTime(command, currentTime)) {
                // Check if this command was used recently
                List<CommandLogger.CommandEntry> recentHistory = commandLogger.getCommandHistory().stream()
                    .filter(entry -> entry.getCommand().equals(command))
                    .filter(entry -> Duration.between(entry.getTimestamp(), currentTime).compareTo(SUGGESTION_COOLDOWN) < 0)
                    .collect(Collectors.toList());
                
                if (recentHistory.isEmpty()) {
                    lastSuggestionTime = currentTime;
                    return command;
                }
            }
        }
        
        return null;
    }
} 