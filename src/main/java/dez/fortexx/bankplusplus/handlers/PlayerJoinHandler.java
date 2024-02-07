package dez.fortexx.bankplusplus.handlers;

import dez.fortexx.bankplusplus.async.SchedulerScope;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.scheduler.IScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinHandler implements Listener {
    private final IScheduledPersistence persistenceProvider;
    private final IScheduler scheduler;

    public PlayerJoinHandler(IScheduledPersistence persistenceProvider, IScheduler scheduler) {
        this.persistenceProvider = persistenceProvider;
        this.scheduler = scheduler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SchedulerScope.from(scheduler)
                .runAsync(scope ->
                        persistenceProvider.loadPlayerSnapshotAsync(scope, e.getPlayer().getUniqueId())
                );
    }
}
