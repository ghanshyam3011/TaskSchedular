# Task Scheduler

A simple command-line task management application written in Java.

## Features
- Create, read, update, and delete tasks
- Store tasks with title, description, and due date
- Command-line interface for task management
- Persistent storage of tasks in a text file

## Project Structure
```
TaskScheduler/
├── src/               # Source code
│   ├── Main.java     # Application entry point
│   ├── Task.java     # Task model class
│   ├── TaskManager.java  # Task management logic
│   ├── CommandHandler.java  # Command processing
├── data/             # Data storage
│   └── tasks.txt     # Task data storage
```

## Setup and Running
1. Ensure you have Java installed on your system
2. Compile the Java files:
   ```
   javac src/*.java
   ```
3. Run the application:
   ```
   java -cp src Main
   ```

## Usage
The application supports the following commands:
- `add` - Add a new task
- `list` - List all tasks
- `update` - Update an existing task
- `delete` - Delete a task
- `help` - Show available commands
- `exit` - Exit the application 