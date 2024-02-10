package dez.fortexx.bankplusplus.api.economy.result;

public record DescribedFailureEconomyResult(
        String description
) implements EconomyResult {
}
