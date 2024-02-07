package dez.fortexx.bankplusplus.persistence.hikari;

import com.zaxxer.hikari.HikariDataSource;
import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import dez.fortexx.bankplusplus.async.IAsyncScope;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.cache.IBankStoreCache;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.BankSnapshot;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;
import dez.fortexx.bankplusplus.persistence.utils.Failure;
import dez.fortexx.bankplusplus.persistence.utils.PersistenceResult;
import dez.fortexx.bankplusplus.persistence.utils.Success;
import dez.fortexx.bankplusplus.persistence.wal.BankDepositOrWithdrawTransaction;
import dez.fortexx.bankplusplus.persistence.wal.BankUpgradeTransaction;
import dez.fortexx.bankplusplus.persistence.wal.IBankStoreWAL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class HikariBankStore implements IBankStore, IScheduledPersistence {
    private record SnapshotWithUUID(
            UUID playerUUID,
            IPlayerBankSnapshot snapshot
    ) {}
    private final HikariDataSource dataSource;
    private final IBankStoreCache bankStoreCache;
    private final IBankStoreWAL wal;
    // TODO: from config
    private final String tableName = "bank";

    public HikariBankStore(
            IBankStoreCache bankStoreCache,
            IBankStoreWAL wal,
            HikariBankStoreConfig config
    ) {
        this.bankStoreCache = bankStoreCache;
        this.wal = wal;
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(config.jdbcUrl());
        dataSource.setUsername(config.username());
        dataSource.setPassword(config.password());

        //TODO: config data source
    }

    @Override
    public boolean addBankFunds(UUID playerUUID, BigDecimal amount) {
        this.wal.insertTransaction(
                playerUUID, new BankDepositOrWithdrawTransaction(amount, TransactionType.DEPOSIT)
        );
        return true;
    }

    @Override
    public boolean takeBankFunds(UUID playerUUID, BigDecimal amount) {
        this.wal.insertTransaction(
                playerUUID, new BankDepositOrWithdrawTransaction(amount, TransactionType.WITHDRAW)
        );
        return true;
    }

    @Override
    public boolean upgradeLevel(UUID playerUUID) {
        this.wal.insertTransaction(
                playerUUID, BankUpgradeTransaction.instance
        );
        return true;
    }

    @Override
    public BigDecimal getBankFunds(UUID playerUUID) {
        final var cacheSnapshot = bankStoreCache.getSnapshot(playerUUID);
        return wal.getBalanceWithLogs(playerUUID, cacheSnapshot);
    }

    @Override
    public int getBankLevel(UUID playerUUID) {
        final var cacheSnapshot = bankStoreCache.getSnapshot(playerUUID);
        return wal.getLevelWithLogs(playerUUID, cacheSnapshot);
    }

    @Override
    public PersistenceResult persistAsync(IAsyncScope scope) {
        final var playersToPersist = wal.dirtyBanks();
        if (playersToPersist.isEmpty())
            return Success.instance;

        final var updatedSnapshots = playersToPersist.stream()
                .map(p -> {
                    final var snapshot = bankStoreCache.getSnapshot(p);
                    if (p == null)
                        return null;
                    final var withWAL = wal.applyWAL(p, snapshot);
                    return new SnapshotWithUUID(p, withWAL);
                })
                .filter(Objects::nonNull)
                .peek(s -> scope.runSync(
                        () -> bankStoreCache.storePlayerSnapshot(s.playerUUID, s.snapshot)
                ))
                .toList();

        try {
            updateFromSnapshots(updatedSnapshots);
        } catch (SQLException e) {
            return new Failure(e.getMessage());
        }
        return Success.instance;
    }

    @Override
    public PersistenceResult loadPlayerSnapshotAsync(IAsyncScope scope, UUID playerUUID) {
        try (Connection connection = dataSource.getConnection()) {
            final var playerSnapshotFromDb = getBankSnapshotFromDatabase(connection, playerUUID);

            if (playerSnapshotFromDb.isEmpty()) {
                createNewPlayer(connection, playerUUID);
            }

            scope.runSync(() ->
                bankStoreCache.storePlayerSnapshot(
                        playerUUID,
                        playerSnapshotFromDb.orElse(BankSnapshot.empty())
                )
            );

            return Success.instance;

        } catch (SQLException e) {
            return new Failure(e.getMessage());
        }
    }

    public PersistenceResult initialize() {
        try (Connection connection = dataSource.getConnection()) {

            createTableIfNotExists(connection);

            return Success.instance;
        } catch (SQLException e) {
            return new Failure(e.getMessage());
        }
    }

    private void createTableIfNotExists(final Connection connection) throws SQLException {
        final var createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (player VARCHAR(128) PRIMARY KEY," +
                "bankLevel INTEGER DEFAULT 1," +
                "balance VARCHAR(256) NOT NULL DEFAULT '0')";

        final var statement = connection.prepareCall(createTableSQL);
        statement.execute();
    }

    private void updateFromSnapshots(Collection<SnapshotWithUUID> snapshots) throws SQLException {
        final var connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        final var updateSql = "UPDATE " + tableName + " SET bankLevel = ?, balance = ? WHERE player = ?";
        final var statement = connection.prepareStatement(updateSql);

        for (final var snapshot : snapshots) {
            statement.setInt(1, snapshot.snapshot.getLevel());
            statement.setString(2, snapshot.snapshot.getBalance().toPlainString());
            statement.setString(3, snapshot.playerUUID.toString());
            statement.addBatch();
        }
        statement.executeBatch();
        connection.commit();
        connection.close();
    }

    private Optional<BankSnapshot> getBankSnapshotFromDatabase(Connection connection, UUID playerUUID) throws SQLException {
        final var readSql = "SELECT bankLevel, balance FROM " + tableName + " WHERE player = ?";
        final var statement = connection.prepareStatement(readSql);
        statement.setString(1, playerUUID.toString());
        final var result = statement.executeQuery();

        final var hasRow = result.next();

        if (!hasRow) {
            return Optional.empty();
        }

        final var bankLevel = result.getInt(1);
        final var balanceString = result.getString(2);
        final var balance = new BigDecimal(balanceString);

        return Optional.of(new BankSnapshot(bankLevel, balance));
    }

    private void createNewPlayer(Connection connection, UUID playerUUID) throws SQLException {
        final var insertSql = "INSERT INTO " + tableName + " (player) VALUES ( ? )";
        final var statement = connection.prepareStatement(insertSql);
        statement.setString(1, playerUUID.toString());

        statement.execute();
    }
}
