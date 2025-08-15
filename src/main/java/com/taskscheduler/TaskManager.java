package com.taskscheduler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TaskManager {
    private static TaskManager instance;
    private List<Task> tasks;
    private final String FILE_NAME = "tasks.json";
    private final Gson gson;

    private TaskManager() {
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create();
            
        tasks = loadTasks();
    }

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public Task getTaskById(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public void saveTasks() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

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
            file.delete();
            return new ArrayList<>();
        }
    }

    private int getNextAvailableId() {
        int maxId = 0;
        for (Task task : tasks) {
            if (task.getId() > maxId) {
                maxId = task.getId();
            }
        }
        return maxId + 1;
    }

    public void addTask(Task task) {
        task.setId(getNextAvailableId());
        tasks.add(task);
        saveTasks();
        System.out.println("Added: " + task);
        
        if (task.getDueDate() != null && !task.isCompleted()) {
            QuartzScheduler.getInstance().scheduleTask(task);
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void deleteTask(int id) {
        boolean removed = tasks.removeIf(task -> task.getId() == id);
        if (removed) {
            System.out.println("Task " + id + " has been deleted.");
            saveTasks();
        } else {
            System.out.println("Task " + id + " not found.");
        }
    }

    public void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    public void completeTask(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.setCompleted(true);
                // Handle recurring task
                if (task.isRecurring()) {
                    // Check if we should generate next occurrence
                    if (task.getRecurrenceCount() > 0) {
                        int remaining = task.getRecurrenceCount() - task.getOccurrencesGenerated();
                        if (remaining > 0 && (task.getRecurrenceEnd() == null || task.getDueDate().isBefore(task.getRecurrenceEnd()))) {
                            Task nextOccurrence = task.generateNextOccurrence();
                            if (nextOccurrence != null) {
                                // Set a new unique ID for the next occurrence
                                nextOccurrence.setId(getNextAvailableId());
                                tasks.add(nextOccurrence);
                                System.out.println("Completed: [" + task.getId() + "] \"" + task.getTitle() + "\"");
                                System.out.println("Generated next occurrence: [" + nextOccurrence.getId() + "] \"" + 
                                                 nextOccurrence.getTitle() + "\" Due: " + 
                                                 nextOccurrence.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                            }
                        }
                    } else if (task.getRecurrenceEnd() == null || task.getDueDate().isBefore(task.getRecurrenceEnd())) {
                        // Handle infinite recurrence (no count specified)
                        Task nextOccurrence = task.generateNextOccurrence();
                        if (nextOccurrence != null) {
                            nextOccurrence.setId(getNextAvailableId());
                            tasks.add(nextOccurrence);
                            System.out.println("Completed: [" + task.getId() + "] \"" + task.getTitle() + "\"");
                            System.out.println("Generated next occurrence: [" + nextOccurrence.getId() + "] \"" + 
                                             nextOccurrence.getTitle() + "\" Due: " + 
                                             nextOccurrence.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        }
                    }
                }
                saveTasks();
                if (!task.isRecurring()) {
                    System.out.println("Task " + id + " marked as completed.");
                }
                return;
            }
        }
        System.out.println("Task not found.");
    }

    // Check for overdue recurring tasks and generate next occurrences
    public void checkRecurringTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasksToAdd = new ArrayList<>();
        
        for (Task task : tasks) {
            if (task.isRecurring() && task.getDueDate() != null) {
                // Check if task is overdue or completed
                if ((!task.isCompleted() && task.getDueDate().isBefore(now)) || 
                    (task.isCompleted() && task.getOccurrencesGenerated() < task.getRecurrenceCount())) {
                    Task nextOccurrence = task.generateNextOccurrence();
                    if (nextOccurrence != null) {
                        tasksToAdd.add(nextOccurrence);
                        System.out.println("Generated next occurrence for recurring task: " + nextOccurrence);
                    }
                }
            }
        }
        
        if (!tasksToAdd.isEmpty()) {
            tasks.addAll(tasksToAdd);
            saveTasks();
        }
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == updatedTask.getId()) {
                tasks.set(i, updatedTask);
                saveTasks();
                return;
            }
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

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.format(formatter));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == null) {
                return null;
            }
            String dateStr = in.nextString();
            return LocalDateTime.parse(dateStr, formatter);
        }
    }
}
