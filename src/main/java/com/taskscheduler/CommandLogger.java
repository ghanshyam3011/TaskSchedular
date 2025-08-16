package com.taskscheduler;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.time.*;
import java.util.*;

public class CommandLogger {
    private static final String LOG_FILE = "command_history.json";
    private final Gson gson;
    private List<CommandEntry> commandHistory;

    public CommandLogger() {
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
        commandHistory = loadCommandHistory();
    }

    public void logCommand(String command) {
        CommandEntry entry = new CommandEntry(command, LocalDateTime.now());
        commandHistory.add(entry);
        saveCommandHistory();
    }

    private void saveCommandHistory() {
        try (Writer writer = new FileWriter(LOG_FILE)) {
            gson.toJson(commandHistory, writer);
        } catch (IOException e) {
            System.out.println("Error saving command history: " + e.getMessage());
        }
    }

    private List<CommandEntry> loadCommandHistory() {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            List<CommandEntry> history = gson.fromJson(reader, new TypeToken<List<CommandEntry>>() {}.getType());
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error loading command history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CommandEntry> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    public static class CommandEntry {
        private String command;
        private LocalDateTime timestamp;

        public CommandEntry(String command, LocalDateTime timestamp) {
            this.command = command;
            this.timestamp = timestamp;
        }

        public String getCommand() {
            return command;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
} 