package dez.fortexx.bankplusplus.utils;

import java.math.BigDecimal;

@FunctionalInterface
public interface ITransactionRounding {
    BigDecimal round(BigDecimal amount);
}
