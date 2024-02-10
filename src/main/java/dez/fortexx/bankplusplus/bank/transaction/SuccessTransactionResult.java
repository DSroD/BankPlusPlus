package dez.fortexx.bankplusplus.bank.transaction;

import java.math.BigDecimal;

public record SuccessTransactionResult(
        BigDecimal newBalance,
        BigDecimal feesPaid
) implements ITransactionResult {
}
