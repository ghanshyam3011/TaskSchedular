package com.taskscheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class QuartzScheduler {
    private static final Logger logger = Logger.getLogger(QuartzScheduler.class.getName());
    private static Scheduler scheduler;
    private static QuartzScheduler instance;

    private QuartzScheduler() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            logger.log(Level.SEVERE, "Failed to initialize Quartz scheduler", e);
        }
    }

    public static synchronized QuartzScheduler getInstance() {
        if (instance == null) {
            instance = new QuartzScheduler();
        }
        return instance;
    }

    public void scheduleTask(Task task) {
        try {
            if (task.getDueDate() == null) {
                logger.warning("Cannot schedule task without due date: " + task.getTitle());
                return;
            }

            JobDetail jobDetail = JobBuilder.newJob(TaskJob.class)
                    .withIdentity(String.valueOf(task.getId()))
                    .usingJobData("taskId", task.getId())
                    .build();

            Trigger trigger;
            
            if (task.isRecurring() && task.getCronExpression() != null) {
                // Use CronTrigger for recurring tasks
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(String.valueOf(task.getId()) + "_trigger")
                        .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()))
                        .build();
            } else {
                // Use SimpleTrigger for one-time tasks
                Date startTime = Date.from(task.getDueDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(String.valueOf(task.getId()) + "_trigger")
                        .startAt(startTime)
                        .build();
            }

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled task: " + task.getTitle() + " for " + task.getDueDate());
        } catch (SchedulerException e) {
            logger.log(Level.SEVERE, "Failed to schedule task: " + task.getTitle(), e);
        }
    }

    public void unscheduleTask(Task task) {
        try {
            scheduler.deleteJob(JobKey.jobKey(String.valueOf(task.getId())));
            logger.info("Unscheduled task: " + task.getTitle());
        } catch (SchedulerException e) {
            logger.log(Level.SEVERE, "Failed to unschedule task: " + task.getTitle(), e);
        }
    }

    public void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            logger.log(Level.SEVERE, "Failed to shutdown scheduler", e);
        }
    }
} 