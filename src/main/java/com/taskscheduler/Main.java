package com.taskscheduler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        CommandHandler commandHandler = new CommandHandler(taskManager);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Task Scheduler Started. Type 'help' for available commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            commandHandler.handleCommands(input);
        }
        
        scanner.close();
    }
} 