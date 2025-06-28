package com.taskscheduler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

public class CommandCompleter implements Completer {
    private final TaskManager taskManager;
    private final List<String> commands = Arrays.asList(
        "add", "list", "delete", "complete", "due", "tag", "untag", 
        "reminder", "email-notification", "help", "exit", "clear", 
        "refresh", "cls", "menu", "suggestions", "settings", "debug",
        "unicode-info"
    );
    
    private final List<String> listCommands = Arrays.asList(
        "upcoming", "overdue", "today", "--tag", "--priority"
    );
    
    private final List<String> recurrenceTypes = Arrays.asList(
        "daily", "weekly", "monthly"
    );

    private static final AttributedStyle SUGGESTION_STYLE = AttributedStyle.DEFAULT.foreground(240); // Light gray

    public CommandCompleter(TaskManager taskManager) {
        this.taskManager = taskManager;
    }


    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        String[] words = buffer.split("\\s+");
        int wordIndex = line.wordIndex();

        if (wordIndex == 0) {
            // Complete command names
            for (String cmd : commands) {
                if (cmd.startsWith(words[0])) {
                    candidates.add(new Candidate(
                        cmd,
                        cmd,
                        null,
                        new AttributedString(cmd, SUGGESTION_STYLE).toAnsi(),
                        null,
                        null,
                        true
                    ));
                }
            }
        } else if (wordIndex == 1) {
            // Handle subcommands for various commands
            if (words[0].equals("list")) {
                // Complete list subcommands
                for (String subcmd : listCommands) {
                    if (subcmd.startsWith(words[1])) {
                        candidates.add(new Candidate(
                            subcmd,
                            subcmd,
                            null,
                            new AttributedString(subcmd, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            } else if (words[0].equals("delete") || words[0].equals("complete") || 
                      words[0].equals("due") || words[0].equals("tag") || 
                      words[0].equals("untag") || words[0].equals("reminder")) {
                // Complete task IDs for these commands
                for (Task task : taskManager.getTasks()) {
                    String id = String.valueOf(task.getId());
                    if (words.length <= 1 || id.startsWith(words[1])) {
                        String description = " (" + truncateString(task.getTitle(), 20) + ")";
                        candidates.add(new Candidate(
                            id,
                            id,
                            null,
                            new AttributedString(id + description, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            } else if (words[0].equals("add")) {
                // For add command, suggest some common flags/parameters
                List<String> addParams = Arrays.asList(
                    "--priority", "--due", "--tag", "--recur", "--notify-email", "--command"
                );
                for (String param : addParams) {
                    if (param.startsWith(words[1])) {
                        candidates.add(new Candidate(
                            param,
                            param,
                            null,
                            new AttributedString(param, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            } else if (words[0].equals("email-notification")) {
                // Could suggest email addresses based on previously used ones
                // This would need to be implemented if email history is tracked
            }
        } else if (wordIndex >= 2) {
            // Handle deeper subcommands and parameters
            
            // Recurrence types for add --recur
            if (words[0].equals("add") && buffer.contains("--recur")) {
                for (String type : recurrenceTypes) {
                    if (words[words.length - 1].isEmpty() || type.startsWith(words[words.length - 1])) {
                        candidates.add(new Candidate(
                            type,
                            type,
                            null,
                            new AttributedString(type, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            }
            // Priority levels for add --priority
            else if (words[0].equals("add") && buffer.contains("--priority")) {
                List<String> priorities = Arrays.asList("high", "medium", "low");
                for (String priority : priorities) {
                    if (words[words.length - 1].isEmpty() || priority.startsWith(words[words.length - 1])) {
                        candidates.add(new Candidate(
                            priority,
                            priority,
                            null,
                            new AttributedString(priority, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            }
            // Tags for list --tag
            else if (words[0].equals("list") && words[1].equals("--tag")) {
                List<String> tags = taskManager.getTasks().stream()
                    .flatMap(task -> task.getTags().stream())
                    .distinct()
                    .collect(Collectors.toList());
                
                for (String tag : tags) {
                    if (words.length <= 2 || tag.startsWith(words[2])) {
                        candidates.add(new Candidate(
                            tag,
                            tag,
                            null,
                            new AttributedString(tag, SUGGESTION_STYLE).toAnsi(),
                            null,
                            null,
                            true
                        ));
                    }
                }
            }
            // Existing tags for tag/untag commands
            else if ((words[0].equals("tag") || words[0].equals("untag")) && words.length >= 3) {
                if (words[0].equals("untag")) {
                    // For untag, show only tags that this task has
                    try {
                        int taskId = Integer.parseInt(words[1]);
                        Task task = taskManager.getTaskById(taskId);
                        if (task != null) {
                            for (String tag : task.getTags()) {
                                if (words.length <= 2 || tag.startsWith(words[words.length - 1])) {
                                    candidates.add(new Candidate(
                                        tag,
                                        tag,
                                        null,
                                        new AttributedString(tag, SUGGESTION_STYLE).toAnsi(),
                                        null,
                                        null,
                                        true
                                    ));
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Invalid task ID, skip suggestions
                    }
                } else {
                    // For tag, show all existing tags as suggestions
                    List<String> existingTags = taskManager.getTasks().stream()
                        .flatMap(task -> task.getTags().stream())
                        .distinct()
                        .collect(Collectors.toList());
                    
                    for (String tag : existingTags) {
                        if (words.length <= 2 || tag.startsWith(words[words.length - 1])) {
                            candidates.add(new Candidate(
                                tag,
                                tag,
                                null,
                                new AttributedString(tag, SUGGESTION_STYLE).toAnsi(),
                                null,
                                null,
                                true
                            ));
                        }
                    }
                }
            }
        }
    }
    
    // Helper method to truncate strings for display
    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
} 