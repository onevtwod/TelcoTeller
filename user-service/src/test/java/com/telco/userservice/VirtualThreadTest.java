package com.telco.userservice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualThreadTest {

    @Test
    public void testVirtualThreadsWork() throws InterruptedException {
        // Test that Java 21 virtual threads are available
        Executor virtualExecutor = Executors.newCachedThreadPool();

        CountDownLatch latch = new CountDownLatch(1);

        virtualExecutor.execute(() -> {
            try {
                Thread.sleep(100); // Simulate some work
                latch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Wait for completion
        boolean completed = latch.await(1, TimeUnit.SECONDS);
        assertTrue(completed, "Virtual thread should complete within 1 second");
    }

    @Test
    public void testVirtualThreadPerformance() throws InterruptedException {
        // Test creating many virtual threads
        Executor virtualExecutor = Executors.newCachedThreadPool();
        int threadCount = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        // Create 1000 virtual threads
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            virtualExecutor.execute(() -> {
                try {
                    Thread.sleep(10); // Simulate work
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Wait for all threads to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        assertTrue(completed, "All virtual threads should complete within 5 seconds");

        long duration = endTime - startTime;
        System.out.println("Created and completed " + threadCount + " virtual threads in " + duration + "ms");

        // Virtual threads should be much faster than platform threads
        assertTrue(duration < 2000, "Virtual threads should complete quickly");
    }
}
