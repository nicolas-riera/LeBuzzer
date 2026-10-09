package com.nicolasgabriel.lebuzzer.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class GameSchedulerService {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> closingTasks = new ConcurrentHashMap<>();

    void scheduleExpiration(String gameCode, long delayMillis, Runnable task) {
        schedule(expirationTasks, gameCode, delayMillis, task);
    }

    void cancelExpiration(String gameCode) {
        cancel(expirationTasks, gameCode);
    }

    void scheduleClosing(String gameCode, long delayMillis, Runnable task) {
        schedule(closingTasks, gameCode, delayMillis, task);
    }

    void cancelClosing(String gameCode) {
        cancel(closingTasks, gameCode);
    }

    void cancelAll(String gameCode) {
        cancelExpiration(gameCode);
        cancelClosing(gameCode);
    }

    private void schedule(Map<String, ScheduledFuture<?>> tasks, String gameCode, long delayMillis, Runnable task) {
        cancel(tasks, gameCode);
        tasks.put(gameCode, executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS));
    }

    private static void cancel(Map<String, ScheduledFuture<?>> tasks, String gameCode) {
        ScheduledFuture<?> task = tasks.remove(gameCode);
        if (task != null) {
            task.cancel(false);
        }
    }
}
