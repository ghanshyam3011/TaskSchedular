
# 📆 NeuroTask Scheduler

Automate, schedule, and track anything—from simple reminders to complex shell commands and scripts. NeuroTask is an intelligent CLI task automation and management application with powerful scheduling, command execution, and emoji-rich interface.

<div align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Platform-CLI-blue?style=for-the-badge" alt="CLI"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker" alt="Docker Ready"/>
</div>

## ✨ Features

- 🚀 **Intuitive Command Line Interface** with colorful prompt and emoji support
- ⚡ **Command Automation** - schedule and automate any shell command, script, or executable (backups, cleanups, deployments, and more)
- 📋 **Task Management** - create, update, complete, and delete tasks effortlessly
- ⏰ **Smart Scheduling** - set precise due dates with natural language processing
- 🔄 **Recurring Tasks** - daily, weekly, or custom recurrence patterns
- 📱 **Email Notifications** - get reminders when tasks are due
- 🤖 **Background Execution** - tasks run even when the app is closed
- 🐳 **Containerized** - run anywhere with Docker, no setup required

## 🚀 Quick Start

First, clone the repository:
```bash
# Clone the repo
git clone https://github.com/ghanshyam3011/TaskSchedular.git
cd TaskSchedular
```

Then, choose one of these options:

### Option 1: Run with Docker (easiest!)
```bash
# Make sure Docker Desktop is running first
# Then run the application with Docker (builds automatically)
./scripts/run-docker.bat
```

**Note:** The Docker script will automatically download the prebuilt JAR file if it's not found locally. This requires an internet connection. If you prefer to build it yourself, you can install Maven and run `mvn clean package` manually before running the Docker command.

### Option 2: Run with Java directly
```bash
# Run the application
./scripts/run-app.bat
```

## 💻 Command Examples

```
📋 NeuroTask > add-command "Database backup" run "mysqldump -u root -p mydb > backup-$(date +%Y%m%d).sql" due "daily at 1am" -r daily

✅ Added command task: "Database backup" due 2025-07-27 01:00 [Priority: ● Medium]
⚙️ Command will execute automatically with system privileges

📋 NeuroTask > add-command "Files backup" run "tar -czf /backups/files_$(date +%Y%m%d).tar.gz /important-data" due "every Sunday at 2am" -r weekly

✅ Added command task: "Files backup" due 2025-07-28 02:00 [Priority: ● Medium]
⚙️ Command will execute automatically with system privileges

📋 NeuroTask > add "Complete project documentation" due tomorrow at 5pm -p high

✅ Added task: "Complete project documentation" due 2025-07-01 17:00 [Priority: ● High]

📋 NeuroTask > list

📊 Task List (3 tasks):
[1] ● Database backup (Completed: false) Due: 2025-07-27 01:00 [Priority: ● Medium] ↻ Recurring: daily ⚙️ Command
[2] ● Files backup (Completed: false) Due: 2025-07-28 02:00 [Priority: ● Medium] ↻ Recurring: weekly ⚙️ Command
[3] ● Complete project documentation (Completed: false) Due: 2025-07-01 17:00 [Priority: ● High]
```

## 📁 Project Structure

```
NeuroTask/
├── 📂 src/                     # Source code
├── 📂 data/                    # Data directory
│   └── 📂 task_outputs/        # Task execution outputs
├── � config/                  # Configuration directory
│   ├── 📄 config.json          # User configuration
│   ├── 📄 tasks.json           # Task data storage
│   └── 📄 email-config.json    # Email settings
├── � docker/                  # Docker configuration
│   ├── 🐳 Dockerfile           # Docker image definition
│   ├── 🐳 docker-compose.yml   # Multi-container setup
│   └── 📜 docker-entrypoint.sh # Container startup script
├── � scripts/                 # Scripts directory
│   ├── �📜 run-app.bat          # Run without Docker
│   ├── 🐳 run-docker.bat       # Run with Docker
│   └── 📜 setup_background_service.ps1 # Windows service setup
├── 📂 docs/                    # Documentation directory
└── 📂 bin/                     # Binary executables
```

## 📝 Command Reference

| Command | Description | Example |
|---------|-------------|---------|
| `add` | Create a new task | `add "Buy groceries" due tomorrow` |
| `add-command` | Schedule a shell command | `add-command "Backup files" run "tar -czf backup.tar.gz /data" due "daily at midnight"` |
| `list` | Show all tasks | `list` or `list upcoming` |
| `complete` | Mark task as done | `complete 1` |
| `delete` | Remove a task | `delete 2` |
| `email-notification` | Set email for notifications | `email-notification user@example.com` |
| `help` | Show available commands | `help` |
| `menu` | Interactive menu mode | `menu` |
| `exit` | Close the application | `exit` |


## 🐳 Docker Magic

Run NeuroTask anywhere without worrying about Java versions, dependencies, or environment variables!

### Why Docker?

- 🛡️ **Isolation**: Everything runs in its own container
- 🧩 **All-in-one**: No Java installation or configuration needed
- 📦 **Portability**: Same experience across Windows, Mac, and Linux
- 🔄 **Persistent**: Your tasks remain between sessions

### Docker Quick Commands

```bash
# Start the application
./run-docker.bat

# For advanced users
docker build -t neurotask .                # Build the image
docker run -it neurotask                   # Run interactively
docker-compose up -d                       # Run with persistent storage
docker-compose down                        # Stop the container
```

## 🔄 Background Service (Windows Only)

NeuroTask Scheduler can run in the background as a Windows service, executing your scheduled tasks even when the application is closed or after system reboot.

### Setting Up Background Service

#### Option 1: Manual Execution (Testing)
```powershell
# Run in a PowerShell window
.\scripts\simple_scheduler.ps1
```
Keep the PowerShell window open - the script will check for tasks every 30 seconds.

#### Option 2: Install as Windows Service (Recommended)
```powershell
# Run PowerShell as Administrator
.\scripts\setup_background_service.ps1
```
This installs and starts a Windows service that runs automatically with Windows.

### Verifying Background Service
1. Create a task scheduled for a future time
2. Close the main application
3. When the scheduled time arrives, the task will execute automatically
4. Check `task_outputs` folder for the execution results

### Viewing Service Status
```powershell
Get-Service -Name NeuroTaskScheduler
```

## 🧩 Advanced Features

- **Automate Anything**: Schedule shell commands or scripts for system maintenance, database backups, deployments, notifications, and more
- **Natural Date Parsing**: Type dates in plain English - "next Friday", "tomorrow at 3pm"
- **Task Priorities**: Flag tasks as High, Medium, or Low priority
- **Command History**: Access previously used commands with up/down arrows
- **Auto-suggestions**: Smart command completion as you type
- **Custom Commands**: Create aliases for frequently used commands

##  Contact

Feel free to open issues on GitHub for bugs, feature requests, or questions.

---

<div align="center">
  <sub>Built with ❤️ by <a href="https://www.linkedin.com/in/ghanshyam-thacker/">Ghanshyam Thacker</a></sub><br>
  <sub><a href="https://github.com/ghanshyam3011/TaskSchedular">https://github.com/ghanshyam3011/TaskSchedular</a></sub>
</div>

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