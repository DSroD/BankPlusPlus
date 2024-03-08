package dez.fortexx.bankplusplus.api.economy.result;

import java.math.BigDecimal;

public record Success(
        BigDecimal newBalance,
        BigDecimal feesPaid
) implements EconomyResult {
}
