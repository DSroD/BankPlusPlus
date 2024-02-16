package dez.fortexx.bankplusplus.persistence.cache;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface IBankStoreCache {
    Optional<IPlayerBankSnapshot> getSnapshot(UUID playerUUID);
    void storePlayerSnapshot(UUID playerUUID, IPlayerBankSnapshot snapshot);
    Optional<Duration> snapshotAge(UUID playerUUID);
}
