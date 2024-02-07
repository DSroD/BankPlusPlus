package dez.fortexx.bankplusplus.persistence;

import java.math.BigDecimal;
import java.util.UUID;

public interface IBankStore {
    boolean addBankFunds(UUID playerUUID, BigDecimal amount);
    boolean takeBankFunds(UUID playerUUID, BigDecimal amount);
    boolean upgradeLevel(UUID playerUUID);
    BigDecimal getBankFunds(UUID playerUUID);
    int getBankLevel(UUID playerUUID);
}
