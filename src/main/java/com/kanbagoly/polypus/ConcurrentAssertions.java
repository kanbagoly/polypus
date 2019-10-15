package com.kanbagoly.polypus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO: Add a documentation with:
// Spot and collect thrown exceptions by the individual threads
// Possible to write fast test (< 1s)
// Threads should start at the same time (limited by the thread pool)
// Have timeout (so and test should fail if it pass)
// No checked exception thrown in the method signatures
// No dependency
// Timeouts and repetition is optional
// TODO: More meaningful method names
// TODO: Java 8 Futures?
public class ConcurrentAssertions {

    private final List<Runnable> tasks;
    private int nTimes = 100;
    private Duration timeout = new Duration(1, TimeUnit.SECONDS);

    private ConcurrentAssertions(List<Runnable> tasks) {
        this.tasks = tasks;
    }

    public ConcurrentAssertions repeatedCalls(int nTimes) {
        this.nTimes = nTimes;
        return this;
    }

    public ConcurrentAssertions timeoutAfter(long timeout, TimeUnit unit) {
        this.timeout = new Duration(timeout, unit);
        return this;
    }

    public void shouldNotThrow() {
        final ThrownExceptions exceptions = new ThrownExceptions();
        execute(executor -> {
            final CountDownLatch blocker = new CountDownLatch(1);
            final CountDownLatch finished = new CountDownLatch(tasks.size() * nTimes);
            for (int i = 1; i <= nTimes; i++) {
                for (Runnable task : tasks) {
                    executor.submit(() -> {
                        try {
                            blocker.await();
                            task.run();
                        } catch (Throwable e) {
                            exceptions.add(e);
                        } finally {
                            finished.countDown();
                        }
                    });
                }
            }
            blocker.countDown();
            await(finished);
        });
        exceptions.assertNotThrown();
    }

    private void execute(Consumer<ExecutorService> runnable) {
        ExecutorService threadPool = Executors.newFixedThreadPool(optimalThreadPoolSize());
        try {
            runnable.accept(threadPool);
        } finally {
            threadPool.shutdownNow();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(timeout.amount, timeout.unit)) {
                String unit = timeout.unit.name().toLowerCase();
                throw new RuntimeException("Timeout after " + timeout.amount + " " + unit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ConcurrentAssertions assertConcurrently(Runnable... tasks) {
        return new ConcurrentAssertions(Arrays.asList(tasks));
    }

    private static int optimalThreadPoolSize() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static class ThrownExceptions {
        private final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        void add(Throwable exception) {
            exceptions.add(exception);
        }
        void assertNotThrown() {
            if (!exceptions.isEmpty()) {
                Throwable head = exceptions.get(0);
                String exceptionsAsString =
                        exceptions.stream().map(Throwable::toString).collect(Collectors.joining(", "));
                throw new RuntimeException("Test failed with the following exception(s): " + exceptionsAsString, head);
            }
        }
    }

    private static class Duration {
        final long amount;
        final TimeUnit unit;
        private Duration(long amount, TimeUnit unit) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Timeout must have positive value");
            }
            this.amount = amount;
            this.unit = unit;
        }
    }

}