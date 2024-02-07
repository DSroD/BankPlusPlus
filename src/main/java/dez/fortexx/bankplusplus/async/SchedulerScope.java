package dez.fortexx.bankplusplus.async;

import dez.fortexx.bankplusplus.scheduler.IScheduler;

import java.util.function.Consumer;

public class SchedulerScope implements IAsyncScope {
    private final IScheduler scheduler;

    private SchedulerScope(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public static SchedulerScope from(IScheduler scheduler) {
        return new SchedulerScope(scheduler);
    }

    @Override
    public void runAsync(Runnable r) {
        scheduler.runAsync(r);
    }

    @Override
    public void runSync(Runnable r) {
        scheduler.runSync(r);
    }

    @Override
    public void runAsync(Consumer<IAsyncScope> r) {
        scheduler.runAsync(() -> r.accept(this));
    }

    @Override
    public void runSync(Consumer<IAsyncScope> r) {
        scheduler.runSync(() -> r.accept(this));
    }

}
