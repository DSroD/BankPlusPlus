package dez.fortexx.bankplusplus.persistence.hikari;

import com.zaxxer.hikari.HikariDataSource;
import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import dez.fortexx.bankplusplus.async.AsyncTask;
import dez.fortexx.bankplusplus.async.BlockingScope;
import dez.fortexx.bankplusplus.async.IAsyncScope;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.api.Failure;
import dez.fortexx.bankplusplus.persistence.api.PersistenceResult;
import dez.fortexx.bankplusplus.persistence.api.Success;
import dez.fortexx.bankplusplus.persistence.cache.IBankStoreCache;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.BankSnapshot;
import dez.fortexx.bankplusplus.persistence.cache.snapshot.IPlayerBankSnapshot;
import dez.fortexx.bankplusplus.persistence.wal.BankDepositOrWithdrawTransaction;
import dez.fortexx.bankplusplus.persistence.wal.BankDowngradeTransaction;
import dez.fortexx.bankplusplus.persistence.wal.BankUpgradeTransaction;
import dez.fortexx.bankplusplus.persistence.wal.IBankStoreWAL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
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
    private final String tableName;
    private final Duration snapshotExpiryDuration;

    public HikariBankStore(
            IBankStoreCache bankStoreCache,
            IBankStoreWAL wal,
            HikariBankStoreConfig config,
            Duration snapshotExpiryDuration
    ) {
        this.bankStoreCache = bankStoreCache;
        this.wal = wal;
        this.snapshotExpiryDuration = snapshotExpiryDuration;
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl(config.jdbcUrl());
        dataSource.setUsername(config.username());
        dataSource.setPassword(config.password());
        tableName = config.tableName();

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
    public boolean downgradeLevel(UUID playerUUID) {
        this.wal.insertTransaction(
                playerUUID, BankDowngradeTransaction.instance
        );
        return true;
    }

    @Override
    public BigDecimal getBankFunds(UUID playerUUID) {
        final var cacheSnapshot = getSnapshotOrLoad(playerUUID);
        if (cacheSnapshot == null) {
            loadPlayerSnapshot(BlockingScope.instance, playerUUID);
        }
        return wal.getBalanceWithLogs(playerUUID, cacheSnapshot);
    }

    @Override
    public int getBankLevel(UUID playerUUID) {
        final var cacheSnapshot = getSnapshotOrLoad(playerUUID);
        return wal.getLevelWithLogs(playerUUID, cacheSnapshot);
    }

    @Override
    public AsyncTask<PersistenceResult> persistAsync() {
        return AsyncTask.of(this::persist);
    }

    private PersistenceResult persist(IAsyncScope scope) {
        final var playersToPersist = wal.dirtyBanks();
        if (playersToPersist.isEmpty())
            return Success.instance;

        final var updatedSnapshots = playersToPersist.stream()
                .map(p -> {
                    if (p == null)
                        return null;
                    final var snapshot = getSnapshotOrLoad(p);
                    final var withWAL = wal.applyWAL(p, snapshot);
                    return new SnapshotWithUUID(p, withWAL);
                })
                .filter(Objects::nonNull)
                .peek(s -> scope.runSync(
                        () -> bankStoreCache.storePlayerSnapshot(s.playerUUID, s.snapshot)
                ))
                .toList();

        return updateFromSnapshots(updatedSnapshots);
    }

    /**
     * Gets snapshot or loads it from db - blocking current thread.
     */
    private IPlayerBankSnapshot getSnapshotOrLoad(UUID playerUUID) {
        return bankStoreCache.getSnapshot(playerUUID)
                .orElseGet(() -> {
                    loadPlayerSnapshot(BlockingScope.instance, playerUUID);
                    return bankStoreCache.getSnapshot(playerUUID).orElseThrow();
                });
    }

    @Override
    public AsyncTask<PersistenceResult> loadPlayerSnapshotAsync(UUID playerUUID) {
        return AsyncTask.of(s -> loadPlayerSnapshot(s, playerUUID));
    }

    private PersistenceResult loadPlayerSnapshot(IAsyncScope scope, UUID playerUUID) {
        // Existing snapshot is up-to-date
        final var snapshotAge = bankStoreCache.snapshotAge(playerUUID);
        if (snapshotAge.map(age -> age.compareTo(snapshotExpiryDuration) < 0).orElse(false)) {
            return Success.instance;
        }

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

    private PersistenceResult updateFromSnapshots(Collection<SnapshotWithUUID> snapshots) {
        try (final var connection = dataSource.getConnection()) {
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
            return Success.instance;
        } catch (SQLException e) {
            return new Failure(e.getMessage());
        }
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
