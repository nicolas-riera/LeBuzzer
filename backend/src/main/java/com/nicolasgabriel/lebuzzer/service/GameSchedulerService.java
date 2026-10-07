package com.nicolasgabriel.lebuzzer.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class GameSchedulerService {

    private final ThreadPoolTaskScheduler scheduler;
    private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();

    public GameSchedulerService() {
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(2);
        this.scheduler.initialize();
    }

    public void scheduleQuestionExpiration(String gameCode, int durationInSeconds, Runnable task) {
        cancelExpiration(gameCode);
        ScheduledFuture<?> future = scheduler.schedule(task, Instant.now().plusSeconds(durationInSeconds));
        expirationTasks.put(gameCode, future);
    }

    public void cancelExpiration(String gameCode) {
        ScheduledFuture<?> task = expirationTasks.remove(gameCode);
        if (task != null) {
            task.cancel(false);
        }
    }
}