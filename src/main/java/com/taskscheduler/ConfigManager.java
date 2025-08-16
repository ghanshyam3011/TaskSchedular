package com.taskscheduler;

import com.google.gson.*;
import java.io.*;
import java.util.*;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, Object> config;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                config = gson.fromJson(reader, Map.class);
            } catch (Exception e) {
                System.out.println("Error loading config: " + e.getMessage());
                config = new HashMap<>();
            }
        } else {
            config = new HashMap<>();
            // Set default values
            config.put("smartSuggestions", true);
            saveConfig();
        }
    }

    private static void saveConfig() {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }

    public static boolean isSmartSuggestionsEnabled() {
        return (boolean) config.getOrDefault("smartSuggestions", true);
    }

    public static void setSmartSuggestionsEnabled(boolean enabled) {
        config.put("smartSuggestions", enabled);
        saveConfig();
    }

    public static void saveEmail(String email) throws IOException {
        config.put("email", email);
        saveConfig();
    }

    public static String getEmail() {
        return (String) config.get("email");
    }
} 