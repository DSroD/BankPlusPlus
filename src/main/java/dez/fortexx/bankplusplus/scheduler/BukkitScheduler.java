package dez.fortexx.bankplusplus.scheduler;

import dez.fortexx.bankplusplus.async.AsyncTask;
import dez.fortexx.bankplusplus.async.SchedulerScope;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class BukkitScheduler implements IScheduler {
    private final Map<UUID, BukkitTask> tasks = new WeakHashMap<>();
    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void cancelAll() {
        tasks.values().forEach(BukkitTask::cancel);
    }

    @Override
    public UUID runAsync(Runnable r) {
        final var task = Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
        final var newUUID = UUID.randomUUID();
        tasks.put(newUUID, task);
        return newUUID;
    }

    @Override
    public UUID runTimerAsync(Runnable r, Duration delay, Duration period) {
        // 1 tick is 50ms on 20 TPS
        final var delayTicks = delay.toMillis() / 50;
        final var periodTicks = period.toMillis() / 50;
        final var task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, r, delayTicks, periodTicks);
        final var newUUID = UUID.randomUUID();
        tasks.put(newUUID, task);
        return newUUID;
    }

    @Override
    public UUID runSync(Runnable r) {
        final var task = Bukkit.getScheduler().runTask(plugin, r);
        final var newUUID = UUID.randomUUID();
        tasks.put(newUUID, task);
        return newUUID;
    }

    @Override
    public UUID runTimer(AsyncTask<?> t, Duration delay, Duration period) {
        // 1 tick is 50ms on 20 TPS
        final var delayTicks = delay.toMillis() / 50;
        final var periodTicks = period.toMillis() / 50;
        final var schedulerScope = SchedulerScope.from(this);
        final var task = Bukkit.getScheduler()
                .runTaskTimer(plugin, () -> t.runInScope(schedulerScope), delayTicks, periodTicks);
        final var newUUID = UUID.randomUUID();
        tasks.put(newUUID, task);
        return newUUID;
    }

    @Override
    public void cancelTask(UUID taskId) {
        final var task = tasks.get(taskId);
        if (task == null)
            return;
        task.cancel();
    }
}
