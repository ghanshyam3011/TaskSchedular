# 📆 Task Scheduler Pro

<div align="center">
  
![Task Scheduler Banner](https://img.shields.io/badge/Task%20Scheduler-Pro-blue?style=for-the-badge&logo=java&logoColor=white)

</div>

<p align="center">
  <b>A powerful Java-based task management system with automatic background execution, command execution, and smart email notifications</b>
</p>

<p align="center">
  <a href="#-key-features">Features</a> •
  <a href="#%EF%B8%8F-installation">Installation</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-background-service">Background Service</a> •
  <a href="#-command-execution">Command Execution</a> •
  <a href="#-email-notifications">Email Notifications</a>
</p>

<div align="center">

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Platform](https://img.shields.io/badge/Platform-Windows-lightblue)

</div>

---

## 🌟 Key Features

- **Interactive Command-Line Interface** with intuitive task management
- **Smart NLP Task Creation** - create tasks with natural language
- **Persistent Storage** of all tasks in JSON format
- **Priority-Based Task Management** (High, Medium, Low)
- **Background Execution System** that runs even when the app is closed
- **Windows Service Integration** for automatic startup with your system
- **Email Notifications** when tasks are completed or due soon
- **Command Execution** capability to run scripts or programs
- **Visual Task Progress** tracking and statistics

## 🛠️ Installation

### Prerequisites
- Java JDK 11 or higher
- Windows OS (for background service functionality)
- PowerShell 5.0 or higher

### Setup Process

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/TaskScheduler.git
   cd TaskScheduler
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

## 🚀 Quick Start

Run the main application using the provided batch file:
```bash
.\run-task-scheduler.bat
```

### Basic Commands
- `add` - Create a new task
- `list` - Show all tasks
- `done` - Mark a task as complete
- `delete` - Remove a task
- `email` - Configure email notifications
- `quit` - Exit the application

### Creating Tasks with Natural Language
```
add Send project report to John by tomorrow at 5pm
add Buy groceries on Saturday morning high
add Call dentist office on Monday at 9am with reminder 30 minutes before
```

## 🔄 Background Service

### Automatic Task Execution
The Task Scheduler can run as a Windows Service, executing tasks in the background even when:
- The main application is closed
- Your system restarts
- You're logged out

### Setup as Windows Service

1. **Run the setup script with administrator privileges**
   ```powershell
   # Run PowerShell as Administrator
   cd path\to\TaskScheduler
   .\setup_background_service.ps1
   ```

2. **Verify the service**
   - Open Services management console (`services.msc`)
   - Look for "Task Scheduler Background Service"
   - Status should show as "Running"

### Managing the Service
```powershell
# Stop the service
nssm stop TaskScheduler

# Start the service
nssm start TaskScheduler

# Remove the service
nssm remove TaskScheduler confirm
```

## 💻 Command Execution

Execute system commands or run scripts automatically when tasks are due:

### Adding Tasks with Commands
```
add Run database backup at 11pm daily command="c:\scripts\backup.bat"
add Update software on Sunday 10am command="powershell -File c:\update.ps1"
```

The command will execute automatically when the task is due, even in background mode.

## 📧 Email Notifications

### Configuration
1. Edit `user-config.json` with your email address:
   ```json
   {
     "email": "your.email@example.com"
   }
   ```

2. Configure email credentials in `email-config.json`:
   ```json
   {
     "username": "your.email@example.com",
     "password": "your-app-password-here", 
     "smtpHost": "smtp.gmail.com",
     "smtpPort": "587"
   }
   ```

> **Security Note**: For Gmail, use an App Password instead of your regular password. 
> Go to Google Account → Security → App passwords to generate one.

### Notification Types
- **Task Due Soon** - Get reminders before tasks are due
- **Task Execution** - Notifications when background tasks execute
- **Task Completion** - Confirmation of completed tasks
- **System Status** - Service start/stop notifications

## 📊 Task Analytics & Monitoring

Track your productivity and task completion with built-in analytics:

- **Task Summary** - Overview of completed, pending, and overdue tasks
- **Progress Tracking** - Visual representation of your productivity
- **Log Files** - Detailed logs for troubleshooting:
  - `scheduler_log.txt` - Main application logs
  - `task_checker_log.txt` - Background execution logs
  - `task_outputs/` - Individual task execution results

## 🔍 Monitoring

### Task Execution Results
Each executed task generates an output file in the `task_outputs/` directory:
```
task_outputs/
├── task_1_20250617_173010.txt
├── task_12_20250518_133200.txt
└── task_25_20250531_230200.txt
```

### Command Output
Commands executed by tasks produce output files that capture:
- Standard output and error streams
- Exit codes
- Execution duration
- Any errors encountered

## 🛡️ Security Features

- **Encrypted Credentials** - Email passwords are never stored in plain text
- **Access Control** - Service runs with limited privileges
- **Error Handling** - Robust error handling prevents system crashes

## ⚙️ Advanced Configuration

### Custom Timing Options
```
# Check for tasks every 60 seconds
$CHECK_INTERVAL = 60

# Set task execution window (in seconds)
$EXECUTION_WINDOW = 60
```

### Email Templates
Customize email notifications by editing template files in the `email-templates/` directory.

## 📚 Additional Documentation

For more detailed information, refer to these additional guides:

- [Background Service Setup Guide](BACKGROUND_SERVICE_GUIDE.md)

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

<div align="center">
  <sub>Built with ❤️ by Task Scheduler Team</sub>
</div>

> **Security Note**: For Gmail, use an App Password instead of your regular password. 
> Go to Google Account → Security → App passwords to generate one.

### Notification Types
- **Task Due Soon** - Get reminders before tasks are due
- **Task Execution** - Notifications when background tasks execute
- **Task Completion** - Confirmation of completed tasks
- **System Status** - Service start/stop notifications

## 📂 Project Structure

```
TaskScheduler/
├── src/                            # Source code
│   └── main/java/com/taskscheduler/
│       ├── Main.java               # Application entry point
│       ├── BackgroundTaskRunner.java # Background execution engine
│       ├── CommandHandler.java     # Command processing
│       ├── EmailNotifier.java      # Email notifications
│       └── ui/                     # User interface components
├── run_task_checker.bat            # Background task execution script
├── simple_scheduler.ps1            # PowerShell task checker loop
├── setup_background_service.ps1    # Windows service setup
├── BACKGROUND_SERVICE_GUIDE.md     # Detailed service documentation
└── tasks.json                      # Task storage (JSON)

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