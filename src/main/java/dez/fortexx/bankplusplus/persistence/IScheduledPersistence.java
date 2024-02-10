package dez.fortexx.bankplusplus.persistence;

import dez.fortexx.bankplusplus.async.AsyncTask;
import dez.fortexx.bankplusplus.persistence.api.PersistenceResult;

import java.util.UUID;

public interface IScheduledPersistence {
    AsyncTask<PersistenceResult> persistAsync();
    AsyncTask<PersistenceResult> loadPlayerSnapshotAsync(UUID uuid);
}
