package com.taskscheduler;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CommandCompleter implements Completer {
    private final TaskManager taskManager;
    private final List<String> commands = Arrays.asList(
        "add", "list", "delete", "complete", "due", "tag", "untag", 
        "reminder", "email-notification", "help", "exit"
    );
    private final List<String> listCommands = Arrays.asList(
        "upcoming", "overdue", "--tag"
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
        } else if (wordIndex == 1 && words.length > 1 && words[0].equals("list")) {
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
        } else if (wordIndex > 1 && words.length > 2 && words[0].equals("add") && buffer.contains("recur")) {
            // Complete recurrence types
            for (String type : recurrenceTypes) {
                if (type.startsWith(words[words.length - 1])) {
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
        } else if (wordIndex > 1 && words.length > 2 && words[0].equals("list") && words[1].equals("--tag")) {
            // Complete tags
            List<String> tags = taskManager.getTasks().stream()
                .flatMap(task -> task.getTags().stream())
                .distinct()
                .collect(Collectors.toList());
            
            for (String tag : tags) {
                if (tag.startsWith(words[words.length - 1])) {
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
        } else if (wordIndex > 1 && words.length > 1 && (words[0].equals("delete") || words[0].equals("complete"))) {
            // Complete task IDs
            for (Task task : taskManager.getTasks()) {
                String id = String.valueOf(task.getId());
                if (id.startsWith(words[words.length - 1])) {
                    candidates.add(new Candidate(
                        id,
                        id,
                        null,
                        new AttributedString(id, SUGGESTION_STYLE).toAnsi(),
                        null,
                        null,
                        true
                    ));
                }
            }
        }
    }
} 