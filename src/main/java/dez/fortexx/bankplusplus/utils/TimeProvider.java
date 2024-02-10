package dez.fortexx.bankplusplus.utils;

import java.time.Instant;

public class TimeProvider implements ITimeProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
