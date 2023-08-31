package com.busted_moments.core.heartbeat;

import com.busted_moments.core.time.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.*;

public class ExecutingThread<T> implements ScheduledFuture<T> {
    private static final Duration MAX_AGE = Duration.of(5, TimeUnit.MINUTES);

    private final ScheduledFuture<T> future;
    private final Date creation;

    public ExecutingThread(ScheduledFuture<T> future) {
        this.future = future;

        this.creation = new Date();
    }


    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return future.getDelay(unit);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return future.compareTo(o);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean shouldCancel() {
        return getAge().greaterThanOrEqual(MAX_AGE);
    }

    public Duration getAge() {
        return Duration.since(creation);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }
}