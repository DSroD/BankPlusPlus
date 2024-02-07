package dez.fortexx.bankplusplus.scheduler;

import java.time.Duration;
import java.util.UUID;

public interface IScheduler {
    void cancelAll();
    UUID runAsync(Runnable r);
    UUID runTimerAsync(Runnable r, Duration delay, Duration period);

    UUID runSync(Runnable r);
    void cancelTask(UUID taskId);

}
