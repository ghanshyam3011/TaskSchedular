package com.taskscheduler;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.time.LocalDateTime;
import java.time.Duration;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TaskManager {
    private List<Task> tasks;
    private final String FILE_NAME = "tasks.json";
    private final Gson gson;

    public TaskManager() {
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();
            
        tasks = loadTasks();
    }

    // Save tasks to JSON file
    public void saveTasks() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // Load tasks from JSON file
    public List<Task> loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            List<Task> loadedTasks = gson.fromJson(reader, new TypeToken<List<Task>>() {}.getType());
            return loadedTasks != null ? loadedTasks : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
            // If there's any error reading the file, delete it and start fresh
            file.delete();
            return new ArrayList<>();
        }
    }

    // Add a new task
    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
        System.out.println("Added: " + task);
    }

    // Get all tasks
    public List<Task> getTasks() {
        return tasks;
    }

    // Delete a task by ID
    public void deleteTask(int id) {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if (removed) {
            System.out.println("Deleted task ID: " + id);
            saveTasks();
        } else {
            System.out.println("Task not found.");
        }
    }

    // List all tasks
    public void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    private static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.toMinutes());
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            if (in.peek() == null) {
                return null;
            }
            long minutes = in.nextLong();
            return Duration.ofMinutes(minutes);
        }
    }
}
