package dez.fortexx.bankplusplus.bank.transaction;

import java.math.BigDecimal;

public record InsufficientFundsTransactionResult(
        BigDecimal maximalTransactionAmountAllowed
) implements  ITransactionResult {}
