package dez.fortexx.bankplusplus.api.economy.transaction;

import java.math.BigDecimal;

public record SuccessTransactionResult(
        BigDecimal newBalance,
        BigDecimal feesPaid
) implements ITransactionResult {
}
