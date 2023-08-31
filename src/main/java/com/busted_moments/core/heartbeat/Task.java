package com.busted_moments.core.heartbeat;

import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.time.Duration;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.busted_moments.client.FuyMain.LOGGER;

public class Task {
    private final Runnable runnable;

    protected final String uuid;
    private final Duration rate;

    private final boolean parallelExecution;
    private boolean suspended = false;

    private final Set<ExecutingThread<?>> ACTIVE_THREADS = ConcurrentHashMap.newKeySet();
    public Date lastExecution = null;


    public Task(Duration rate, Runnable runnable, boolean parallelExecution, Function<Task, String> register) {
        this.runnable = runnable;
        this.rate = rate;

        this.parallelExecution = parallelExecution;

        this.uuid = register.apply(this);
    }

    public Task(Duration rate, Runnable runnable, boolean parallelExecution) {
        this(rate, runnable, parallelExecution, Heartbeat::register);
    }


    public Task(Duration rate, Runnable runnable, Function<Task, String> register) {
        this(rate, runnable, false, register);
    }

    public Task(Duration rate, Runnable runnable) {
        this(rate, runnable, Heartbeat::register);
    }

    public Task(Schedule annotation, Runnable runnable, Function<Task, String> register) {
        this(Duration.of(annotation.rate(), annotation.unit()), runnable, annotation.parallel(), register);
    }

    public Task(Schedule annotation, Runnable runnable) {
        this(annotation, runnable, Heartbeat::register);
    }

    public Duration getRate() {
        return rate;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public Set<ExecutingThread<?>> getActiveThreads() {
        return ACTIVE_THREADS;
    }

    public int countActiveThreads() {
        return getActiveThreads().size();
    }

    public boolean shouldExecute() {
        return !suspended &&
                (countActiveThreads() == 0 || (parallelExecution && countActiveThreads() < 4))
                && (lastExecution == null || Duration.since(lastExecution).greaterThanOrEqual(rate));
    }

    public void cullInactiveThreads() {
        getActiveThreads().removeIf((thread) -> {
            if (thread.shouldCancel()) {
                LOGGER.error("Thread has exceeded its max age! ({})", thread.getAge().toString());

                thread.cancel(true);
                if (getActiveThreads().size() == 1) setLastExecution(new Date());
            }

            return thread.isCancelled() || thread.isDone();
        });
    }

    public void setLastExecution(Date date) {
        this.lastExecution = date;
    }

    public void suspend() {
        this.suspended = true;
    }

    public void resume() {
        this.suspended = false;
    }
}
