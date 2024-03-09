package dez.fortexx.bankplusplus.persistence.wal;

import dez.fortexx.bankplusplus.persistence.cache.snapshot.BankSnapshot;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TODO: unit tests

/**
 * In-memory WAL for storing player transactions
 */
public class BankStoreWAL implements IBankStoreWAL {
    private final ConcurrentMap<UUID, List<ITransactionLog>> playerTransactions = new ConcurrentHashMap<>();

    @Override
    public void insertTransaction(UUID playerUUID, ITransactionLog log) {
        final var list = playerTransactions.getOrDefault(playerUUID, new LinkedList<>());
        list.add(log);
        playerTransactions.put(playerUUID, list);
    }

    @Override
    public BigDecimal getBalanceWithLogs(UUID playerUUID, IPlayerBankSnapshot snapshot) {
        final var transactions = playerTransactions.get(playerUUID);
        final var snapshotBalance = snapshot.getBalance();

        if (transactions == null) {
            return snapshotBalance;
        }

        var finalBalance = snapshotBalance;
        for (final var transaction : transactions) {
            if (transaction instanceof BankDepositOrWithdrawTransaction t) {
                finalBalance = switch (t.type()) {
                    case DEPOSIT -> finalBalance.add(t.amount());
                    case WITHDRAW -> finalBalance.subtract(t.amount());
                };
            }
        }

        return finalBalance;
    }

    @Override
    public int getLevelWithLogs(UUID playerUUID, IPlayerBankSnapshot snapshot) {
        final var transactions = playerTransactions.get(playerUUID);
        final var snapshotLevel = snapshot.getLevel();

        if (transactions == null) {
            return snapshotLevel;
        }

        var finalLevel = snapshotLevel;
        for (final var transaction : transactions) {
            if (transaction == BankUpgradeTransaction.instance) {
                finalLevel++;
            }
            else if (transaction == BankDowngradeTransaction.instance) {
                finalLevel--;
            }
        }

        return finalLevel;
    }

    @Override
    public Set<UUID> dirtyBanks() {
        return playerTransactions.keySet();
    }

    @Override
    public IPlayerBankSnapshot applyWAL(UUID playerUUID, IPlayerBankSnapshot snapshot) {
        final var transactions = playerTransactions.remove(playerUUID);
        if (transactions == null) {
            return snapshot;
        }

        var levelAcc = snapshot.getLevel();
        var balanceAcc = snapshot.getBalance();
        for (final var transaction : transactions) {
            if (transaction instanceof BankDepositOrWithdrawTransaction t) {
                balanceAcc = switch (t.type()) {
                    case DEPOSIT -> balanceAcc.add(t.amount());
                    case WITHDRAW -> balanceAcc.subtract(t.amount());
                };
            }

            if (transaction == BankUpgradeTransaction.instance) {
                levelAcc++;
            } else if (transaction == BankDowngradeTransaction.instance) {
                levelAcc--;
            }
        }

        return new BankSnapshot(levelAcc, balanceAcc);
    }
}
