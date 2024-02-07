package dez.fortexx.bankplusplus.persistence.wal;

public final class BankUpgradeTransaction implements ITransactionLog {
    public static final BankUpgradeTransaction instance = new BankUpgradeTransaction();
    private BankUpgradeTransaction() {}
}
