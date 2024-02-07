package dez.fortexx.bankplusplus.persistence.wal;

import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;

import java.math.BigDecimal;

public record BankDepositOrWithdrawTransaction(
        BigDecimal amount,
        TransactionType type
) implements ITransactionLog {
}
