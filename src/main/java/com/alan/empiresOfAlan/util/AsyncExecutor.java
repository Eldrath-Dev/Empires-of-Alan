package com.alan.empiresOfAlan.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Utility class for handling async operations
 */
public class AsyncExecutor {
    private final JavaPlugin plugin;
    private final Executor executor;

    public AsyncExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(3); // Create a small thread pool
    }

    /**
     * Run a task asynchronously
     *
     * @param task The task to run
     * @param <T> The return type of the task
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> runAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }

    /**
     * Run a task asynchronously with no return value
     *
     * @param task The task to run
     * @return CompletableFuture that completes when the task is done
     */
    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    /**
     * Run a task on the main thread
     *
     * @param task The task to run
     * @param <T> The return type of the task
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> runSync(Supplier<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(task.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Run a task on the main thread with no return value
     *
     * @param task The task to run
     * @return CompletableFuture that completes when the task is done
     */
    public CompletableFuture<Void> runSync(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Shutdown the executor
     */
    public void shutdown() {
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdown();
        }
    }
}