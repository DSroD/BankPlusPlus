package dez.fortexx.bankplusplus.persistence.cache.snapshot;

import java.math.BigDecimal;

public interface IPlayerBankSnapshot {
    int getLevel();

    BigDecimal getBalance();
}
