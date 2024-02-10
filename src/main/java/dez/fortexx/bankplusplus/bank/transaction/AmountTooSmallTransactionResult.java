package dez.fortexx.bankplusplus.bank.transaction;

public final class AmountTooSmallTransactionResult implements ITransactionResult {
    public static final AmountTooSmallTransactionResult instance = new AmountTooSmallTransactionResult();
    private AmountTooSmallTransactionResult() {}
}
