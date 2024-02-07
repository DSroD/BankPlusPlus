package dez.fortexx.bankplusplus.persistence.cache.snapshot;

import java.math.BigDecimal;

public class ModifiableBankSnapshot implements IPlayerBankSnapshot {
    private int level;
    private BigDecimal balance;

    public ModifiableBankSnapshot(int level, BigDecimal balance) {
        this.level = level;
        this.balance = balance;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    public void setFunds(BigDecimal balance) {
        this.balance = balance;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
