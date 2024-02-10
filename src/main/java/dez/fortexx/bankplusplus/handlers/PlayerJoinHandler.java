package dez.fortexx.bankplusplus.handlers;

import dez.fortexx.bankplusplus.async.IAsyncScope;
import dez.fortexx.bankplusplus.async.SchedulerScope;
import dez.fortexx.bankplusplus.logging.ILogger;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.api.Failure;
import dez.fortexx.bankplusplus.persistence.api.PersistenceResult;
import dez.fortexx.bankplusplus.persistence.api.Success;
import dez.fortexx.bankplusplus.scheduler.IScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class PlayerJoinHandler implements Listener {
    private final IScheduledPersistence persistenceProvider;
    private final IScheduler scheduler;
    private final ILogger logger;

    public PlayerJoinHandler(IScheduledPersistence persistenceProvider, IScheduler scheduler, ILogger logger) {
        this.persistenceProvider = persistenceProvider;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        persistenceProvider.loadPlayerSnapshotAsync(e.getPlayer().getUniqueId())
                .then(log(e.getPlayer().getName()))
                .runInScope(SchedulerScope.from(scheduler));
    }

    @Contract(pure = true)
    private @NotNull BiConsumer<IAsyncScope, PersistenceResult> log(String playerName) {
        return (scope, result) -> scope.runSync(() -> {
            if (result instanceof Failure f) {
                logger.severe("Could not load bank snapshot of player " + playerName);
                logger.severe(f.message());
                return;
            }
            if (result instanceof Success) {
                logger.info("Loaded bank snapshot of player " + playerName);
            }

        });
    }
}
