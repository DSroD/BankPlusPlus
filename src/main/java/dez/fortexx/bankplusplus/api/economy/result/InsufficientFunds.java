package dez.fortexx.bankplusplus.api.economy.result;

import java.math.BigDecimal;

public record InsufficientFunds(
        BigDecimal maximalTransactionAmountAllowed
) implements EconomyResult {
}
