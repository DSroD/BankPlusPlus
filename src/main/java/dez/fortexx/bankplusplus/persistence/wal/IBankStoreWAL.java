package dez.fortexx.bankplusplus.persistence.wal;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public interface IBankStoreWAL {
    void insertTransaction(UUID playerUUID, ITransactionLog log);
    BigDecimal getBalanceWithLogs(UUID playerUUID, IPlayerBankSnapshot snapshot);
    int getLevelWithLogs(UUID playerUUID, IPlayerBankSnapshot snapshot);
    Set<UUID> dirtyBanks();

    /**
     * Pops WAL of given player and returns snapshot after WAL modifications
     * @param snapshot
     * @return
     */
    IPlayerBankSnapshot applyWAL(UUID playerUUID, IPlayerBankSnapshot snapshot);
}
