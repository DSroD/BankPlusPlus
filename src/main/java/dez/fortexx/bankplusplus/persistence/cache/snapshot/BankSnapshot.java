package dez.fortexx.bankplusplus.persistence.cache.snapshot;

import java.math.BigDecimal;

public record BankSnapshot(
        int level,
        BigDecimal balance
) implements IPlayerBankSnapshot {
    private final static BankSnapshot DEFAULT_BANK_SNAPSHOT = new BankSnapshot(1, BigDecimal.ZERO);
    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    public static BankSnapshot empty() {
        return DEFAULT_BANK_SNAPSHOT;
    }
}
