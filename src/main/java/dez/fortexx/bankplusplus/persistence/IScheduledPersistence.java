package dez.fortexx.bankplusplus.persistence;

import dez.fortexx.bankplusplus.async.IAsyncScope;
import dez.fortexx.bankplusplus.persistence.utils.PersistenceResult;

import java.util.UUID;

public interface IScheduledPersistence {
    PersistenceResult persistAsync(IAsyncScope scope);
    PersistenceResult loadPlayerSnapshotAsync(IAsyncScope scope, UUID uuid);
}
