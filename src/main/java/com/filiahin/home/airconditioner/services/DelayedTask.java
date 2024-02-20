package com.filiahin.home.airconditioner.services;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DelayedTask implements Runnable {
    private volatile boolean isRunning = false;
    private volatile boolean stopRequested = false;
    private long startTime;
    private final long duration;
    private final CountDownLatch latch;
    private final Consumer<Void> afterTask;

    public DelayedTask(long duration, Consumer<Void> afterTask) {
        this.duration = duration;
        this.afterTask = afterTask;
        this.latch = new CountDownLatch(1);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getRemainingTime() {
        if (isRunning) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            return (int) Math.max(duration - elapsedTime, 0) / 1000;
        }
        return 0; // Task is not running
    }

    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        isRunning = true;
        stopRequested = false;
        try {
            while (!stopRequested) {
                if (System.currentTimeMillis() - startTime >= duration) {
                    break;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            isRunning = false;
            latch.countDown();
            if (!stopRequested) {
                afterTask.accept(null);
            }
        }
    }
}
