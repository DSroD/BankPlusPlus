package dez.fortexx.bankplusplus.utils.caching;

import dez.fortexx.bankplusplus.utils.ITimeProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class TTLCachedValue<T> implements ICachedValue<T> {
    private final Duration ttl;
    private final ITimeProvider timeProvider;
    private final Supplier<T> newValueSupplier;
    private Instant expiryTime;
    private T value;

    public TTLCachedValue(
            Supplier<T> newValueSupplier,
            Duration ttl,
            ITimeProvider timeProvider
    ) {
        this.ttl = ttl;
        this.timeProvider = timeProvider;
        this.newValueSupplier = newValueSupplier;
        this.value = newValueSupplier.get();
        this.expiryTime = timeProvider.now().plus(ttl);
    }

    @Override
    public T get() {
        final var now = timeProvider.now();
        if (now.isAfter(expiryTime)) {
            value = newValueSupplier.get();
            expiryTime = now.plus(ttl);
        }
        return value;
    }

    @Override
    public void invalidate() {
        expiryTime = timeProvider.now();
    }
}
