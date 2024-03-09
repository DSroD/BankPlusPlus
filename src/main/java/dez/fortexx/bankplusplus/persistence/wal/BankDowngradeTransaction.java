package dez.fortexx.bankplusplus.persistence.wal;

public final class BankDowngradeTransaction implements ITransactionLog {
    public static BankDowngradeTransaction instance = new BankDowngradeTransaction();
    private BankDowngradeTransaction() {}
}
