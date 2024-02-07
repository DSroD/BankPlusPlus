package dez.fortexx.bankplusplus.persistence.cache;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.ModifiableBankSnapshot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class LRUBankStoreCache implements IBankStoreCache {
    Map<UUID, ModifiableBankSnapshot> snapshots;

    public LRUBankStoreCache(int maxSize) {
        snapshots = new LinkedHashMap<>(maxSize * 4 / 3, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, ModifiableBankSnapshot> eldest) {
                return size() > maxSize;
            }
        };
    }

    @Override
    public IPlayerBankSnapshot getSnapshot(UUID playerUUID) {
        return snapshots.get(playerUUID);
    }

    @Override
    public void storePlayerSnapshot(UUID player, IPlayerBankSnapshot snapshot) {
        if (snapshots.containsKey(player)) {
            final var existingSnapshot = snapshots.get(player);
            existingSnapshot.setLevel(snapshot.getLevel());
            existingSnapshot.setFunds(snapshot.getBalance());
            return;
        }

        final var newSnapshot = new ModifiableBankSnapshot(snapshot.getLevel(), snapshot.getBalance());
        snapshots.put(player, newSnapshot);
    }
}
