package dez.fortexx.bankplusplus.api.economy.result;

public final class AmountTooSmall implements EconomyResult {
    public static final AmountTooSmall instance = new AmountTooSmall();

    private AmountTooSmall() {}
}
