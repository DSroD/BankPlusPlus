package dez.fortexx.bankplusplus.api.economy.result;

public final class LimitViolation implements EconomyResult {
    public static LimitViolation instance = new LimitViolation();

    private LimitViolation() {}
}
