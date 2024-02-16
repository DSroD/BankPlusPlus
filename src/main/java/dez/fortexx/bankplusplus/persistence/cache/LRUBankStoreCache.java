package dez.fortexx.bankplusplus.persistence.cache;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.ModifiableBankSnapshot;
import dez.fortexx.bankplusplus.utils.ITimeProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

// TODO: unit tests

/**
 * LRU cache for bank snapshots
 */
public class LRUBankStoreCache implements IBankStoreCache {
    private final Map<UUID, ModifiableBankSnapshot> snapshots;
    private final WeakHashMap<UUID, Instant> lastSnapshotUpdate = new WeakHashMap<>();
    private final ITimeProvider timeProvider;

    public LRUBankStoreCache(int maxSize, ITimeProvider timeProvider) {
        snapshots = new LinkedHashMap<>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, ModifiableBankSnapshot> eldest) {
                return size() > maxSize;
            }
        };
        this.timeProvider = timeProvider;
    }

    @Override
    public Optional<IPlayerBankSnapshot> getSnapshot(UUID playerUUID) {
        return Optional.ofNullable(snapshots.get(playerUUID));
    }

    @Override
    public void storePlayerSnapshot(UUID playerUUID, IPlayerBankSnapshot snapshot) {
        if (snapshots.containsKey(playerUUID)) {
            final var existingSnapshot = snapshots.get(playerUUID);
            existingSnapshot.setLevel(snapshot.getLevel());
            existingSnapshot.setFunds(snapshot.getBalance());
            return;
        }

        final var newSnapshot = new ModifiableBankSnapshot(snapshot.getLevel(), snapshot.getBalance());
        snapshots.put(playerUUID, newSnapshot);
        lastSnapshotUpdate.put(playerUUID, timeProvider.now());
    }

    @Override
    public Optional<Duration> snapshotAge(UUID playerUUID) {
        return Optional.ofNullable(lastSnapshotUpdate.get(playerUUID))
                .map(update -> Duration.between(update, timeProvider.now()));
    }
}
