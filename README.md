# Task Scheduler

A robust command-line task management application written in Java with background task execution capabilities.

## Features
- Create, read, update, and delete tasks
- Store tasks with title, description, and due date
- Command-line interface for task management
- Persistent storage of tasks in JSON format
- Background task execution (even when main application is closed)
- Email notifications for task completion
- Automated task scheduling with custom timing options

## Project Structure
```
TaskScheduler/
├── src/main/java/com/taskscheduler/  # Source code
│   ├── Main.java                    # Application entry point
│   ├── Task.java                    # Task model class
│   ├── TaskManager.java             # Task management logic
│   ├── CommandHandler.java          # Command processing
│   ├── BackgroundTaskRunner.java    # Background task execution
│   ├── EmailNotifier.java           # Email notification service
│   └── ConfigManager.java           # Configuration management
├── simple_scheduler.ps1             # PowerShell background scheduler
├── start_scheduler.bat              # Script to start background scheduler
├── run_task_checker.bat             # Task execution batch file
├── task_outputs/                    # Task execution output files
└── tasks.json                       # Task data storage (JSON format)
```

## Setup and Running

### Basic Setup
1. Ensure you have Java installed on your system
2. Compile the application:
   ```
   javac -d classes -cp . src/main/java/com/taskscheduler/*.java
   ```
3. Run the application:
   ```
   java -cp classes com.taskscheduler.Main
   ```

### Email Notifications Setup
To enable email notifications:

1. Set your email address in `user-config.json`:
   ```json
   {
     "email": "your.email@example.com"
   }
   ```

2. Edit the `run_task_checker.bat` file to add your email credentials:
   ```bat
   set EMAIL_USERNAME=your.email@example.com
   set EMAIL_PASSWORD=your-app-password-here
   ```
   
   Note: For Gmail, you'll need to use an App Password rather than your regular password.
   
3. Email notifications will be sent when tasks are executed by the background scheduler
   }
   ```

2. Configure email credentials in `run_task_checker.bat`:
   ```bat
   set EMAIL_USERNAME=your.email@example.com
   set EMAIL_PASSWORD=your-app-password-here
   ```

3. For Gmail users, you'll need to create an App Password:
   - Go to your Google Account settings
   - Navigate to Security > App passwords
   - Create a new app password for "Mail" and use it as your EMAIL_PASSWORD

## Background Task Execution

This Task Scheduler can run in the background and execute tasks even when the main application is closed. The background execution system consists of:

1. **PowerShell Scheduler (`simple_scheduler.ps1`)**: Runs every minute to check for due tasks
2. **Launcher Script (`start_scheduler.bat`)**: Starts the background scheduler
3. **Task Checker (`run_task_checker.bat`)**: Executes due tasks through the Java application

### Starting the Background Scheduler

To start the background task scheduler:

1. Run the `start_scheduler.bat` file:
   ```
   .\start_scheduler.bat
   ```

2. The scheduler will start in a console window and begin checking for due tasks every minute.
3. You can close the main application, and tasks will continue to execute at their scheduled times.
4. Email notifications will be sent upon task completion (if configured).

### Logs and Monitoring

Background task execution generates several log files:

- `scheduler_log.txt`: Records when the scheduler checks for tasks
- `task_checker_log.txt`: Details about task execution attempts
- `task_outputs/`: Contains output files from executed tasks

## Usage
The application supports the following commands:
- `add` - Add a new task with optional due date and recurrence
- `list` - List all tasks
- `list upcoming` - List upcoming tasks
- `list overdue` - List overdue tasks
- `delete` - Delete a task
- `complete` - Mark a task as completed
- `due` - Set or change the due date for a task
- `tag` - Add tags to a task
- `reminder` - Set a reminder for a task
- `email-notification` - Configure email notifications
- `help` - Show available commands
- `exit` - Exit the application

## 🐳 Docker Support

Run NeuroTask anywhere without worrying about dependencies or environment setup.

### Prerequisites
- Docker installed on your system ([Get Docker](https://docs.docker.com/get-docker/))

### Quick Start with Docker

1. **Build the Docker image:**
   ```bash
   docker build -t neurotask .
   ```

2. **Run the container:**
   ```bash
   docker run -it neurotask
   ```

### Using Docker Compose (Recommended)

For easier management with persistent data:

1. **Start the application:**
   ```bash
   docker-compose up -d
   ```

2. **View logs:**
   ```bash
   docker-compose logs -f
   ```

3. **Stop the application:**
   ```bash
   docker-compose down
   ```

### Benefits of Containerization

- **Consistent Environment**: Runs exactly the same way on any system
- **Zero Dependencies**: No need to install Java or configure environment variables
- **Portable**: Easy to ship and deploy anywhere
- **Isolated**: Container keeps the application self-contained
- **Scalable**: Ready for cloud deployment and orchestration