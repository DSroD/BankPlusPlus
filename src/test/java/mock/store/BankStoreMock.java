package mock.store;

import dez.fortexx.bankplusplus.persistence.IBankStore;

import java.math.BigDecimal;
import java.util.UUID;

public class BankStoreMock implements IBankStore {
    private BigDecimal money = BigDecimal.ZERO;
    private int level = 1;
    @Override
    public boolean addBankFunds(UUID playerUUID, BigDecimal amount) {
        this.money = this.money.add(amount);
        return true;
    }

    @Override
    public boolean takeBankFunds(UUID playerUUID, BigDecimal amount) {
        this.money = this.money.subtract(amount);
        return true;
    }

    @Override
    public boolean upgradeLevel(UUID playerUUID) {
        this.level = this.level + 1;
        return true;
    }

    @Override
    public BigDecimal getBankFunds(UUID playerUUID) {
        return money;
    }

    @Override
    public int getBankLevel(UUID playerUUID) {
        return level;
    }
}
