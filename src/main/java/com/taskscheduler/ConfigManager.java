package com.taskscheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String CONFIG_FILE = "user-config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveEmail(String email) throws IOException {
        Map<String, String> config = new HashMap<>();
        config.put("email", email);
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        }
    }

    public static String getEmail() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return null;

        try (Reader reader = new FileReader(file)) {
            Map<String, String> config = gson.fromJson(reader, Map.class);
            return config.get("email");
        } catch (IOException e) {
            return null;
        }
    }
} 