package dez.fortexx.bankplusplus.persistence.cache;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;

import java.util.UUID;

public interface IBankStoreCache {
    IPlayerBankSnapshot getSnapshot(UUID playerUUID);
    void storePlayerSnapshot(UUID player, IPlayerBankSnapshot snapshot);
}
