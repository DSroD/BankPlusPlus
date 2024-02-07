package dez.fortexx.bankplusplus.persistence.wal;

public sealed interface ITransactionLog permits BankDepositOrWithdrawTransaction, BankUpgradeTransaction {
}
