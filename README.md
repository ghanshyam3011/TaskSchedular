# 📆 Task Scheduler

An intelligent CLI task management application with powerful scheduling capabilities and emoji-rich interface.

<div align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Platform-CLI-blue?style=for-the-badge" alt="CLI"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker" alt="Docker Ready"/>
</div>

## ✨ Features

- 🚀 **Intuitive Command Line Interface** with colorful prompt and emoji support
- 📋 **Task Management** - create, update, complete, and delete tasks effortlessly
- ⏰ **Smart Scheduling** - set precise due dates with natural language processing
- 🔄 **Recurring Tasks** - daily, weekly, or custom recurrence patterns
- 📱 **Email Notifications** - get reminders when tasks are due
- 🤖 **Background Execution** - tasks run even when the app is closed
- 🐳 **Containerized** - run anywhere with Docker, no setup required

## 🚀 Quick Start

### Option 1: Run with Docker (easiest!)
```bash
# Clone the repo
git clone https://github.com/username/TaskSchedular.git
cd TaskSchedular

# Run with Docker (builds automatically)
./run-docker.bat
```

### Option 2: Run with Java directly
```bash
# Clone the repo
git clone https://github.com/username/TaskSchedular.git
cd TaskSchedular

# Run the application
./run-app.bat
```

## 💻 Command Examples

```
📋 TaskScheduler > add "Complete project documentation" due tomorrow at 5pm -p high

✅ Added task: "Complete project documentation" due 2025-07-01 17:00 [Priority: ● High]

📋 TaskScheduler > add "Team meeting" due "every Monday at 10am" -r weekly -e

✅ Added recurring task: "Team meeting" due 2025-07-07 10:00 [Priority: ● Medium]
📧 Email notifications enabled for this task

📋 TaskScheduler > list

📊 Task List (2 tasks):
[1] ● Complete project documentation (Completed: false) Due: 2025-07-01 17:00 [Priority: ● High]
[2] ● Team meeting (Completed: false) Due: 2025-07-07 10:00 [Priority: ● Medium] ↻ Recurring: weekly
```

## 📁 Project Structure

```
TaskScheduler/
├── 📂 src/                     # Source code
├── 📂 task_outputs/            # Task execution outputs
├── 📄 config.json              # User configuration
├── 📄 tasks.json               # Task data storage
├── 📄 email-config.json        # Email settings
├── 🐳 Dockerfile               # Docker configuration
├── 🐳 docker-compose.yml       # Multi-container setup
├── 📜 run-app.bat              # Run without Docker
└── 🐳 run-docker.bat           # Run with Docker
```

## 📝 Command Reference

| Command | Description | Example |
|---------|-------------|---------|
| `add` | Create a new task | `add "Buy groceries" due tomorrow` |
| `list` | Show all tasks | `list` or `list upcoming` |
| `complete` | Mark task as done | `complete 1` |
| `delete` | Remove a task | `delete 2` |
| `email-notification` | Set email for notifications | `email-notification user@example.com` |
| `help` | Show available commands | `help` |
| `menu` | Interactive menu mode | `menu` |
| `exit` | Close the application | `exit` |


## 🐳 Docker Magic

Run TaskScheduler anywhere without worrying about Java versions, dependencies, or environment variables!

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
docker build -t taskscheduler .                # Build the image
docker run -it taskscheduler                   # Run interactively
docker-compose up -d                           # Run with persistent storage
docker-compose down                            # Stop the container
```

## 🧩 Advanced Features

- **Natural Date Parsing**: Type dates in plain English - "next Friday", "tomorrow at 3pm"
- **Task Priorities**: Flag tasks as High, Medium, or Low priority
- **Command History**: Access previously used commands with up/down arrows
- **Auto-suggestions**: Smart command completion as you type
- **Custom Commands**: Create aliases for frequently used commands

## 🔍 More Info

For advanced configuration options, background service setup, and API documentation, check out the source code and comments.

## 📬 Contact

Feel free to open issues on GitHub for bugs, feature requests, or questions.

---

<div align="center">
  <sub>Built with ❤️ using Java and Docker</sub>
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