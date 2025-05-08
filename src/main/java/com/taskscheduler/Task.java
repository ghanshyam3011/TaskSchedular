package com.taskscheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class Task {
    
    private static int idCounter = 1;
    private int id;
    private String title;
    private boolean completed;
    private LocalDateTime dueDate;
    private Set<String> tags;
    private boolean notified;
    private Duration reminderTime;

    public Task(int id, String title, boolean completed, LocalDateTime dueDate) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.dueDate = dueDate;
        this.tags = new HashSet<>();
        this.notified = false;
        this.reminderTime = Duration.ofHours(1); // Default reminder: 1 hour before
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Set<String> getTags() {
        if (tags == null) {
            tags = new HashSet<>();
        }
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        this.tags.add(tag.toLowerCase());
    }

    public void removeTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
            return;
        }
        this.tags.remove(tag.toLowerCase());
    }

    public boolean hasTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
            return false;
        }
        return this.tags.contains(tag.toLowerCase());
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public boolean isDue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !completed;
    }

    public Duration getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Duration reminderTime) {
        this.reminderTime = reminderTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%d] %s (Completed: %s)", id, title, completed));
        
        if (dueDate != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                sb.append(" Due: ").append(dueDate.format(formatter));
            } catch (Exception e) {
                sb.append(" Due: [invalid date]");
            }
        }

        if (tags != null && !tags.isEmpty()) {
            sb.append(" Tags: ").append(String.join(", ", tags));
        }

        return sb.toString();
    }
}

