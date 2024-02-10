package dez.fortexx.bankplusplus.scheduler;

import dez.fortexx.bankplusplus.async.AsyncTask;

import java.time.Duration;
import java.util.UUID;

public interface IScheduler {
    void cancelAll();
    UUID runAsync(Runnable r);

    UUID runTimerAsync(Runnable r, Duration delay, Duration period);

    /**
     * Runs task that blocks the owner of this async scope
     * @param r
     * @return
     */
    UUID runSync(Runnable r);

    /**
     * Creates a timer that runs an async task each its tick
     * @return UUID of the timer task
     */
    UUID runTimer(AsyncTask<?> t, Duration delay, Duration period);
    void cancelTask(UUID taskId);

}
