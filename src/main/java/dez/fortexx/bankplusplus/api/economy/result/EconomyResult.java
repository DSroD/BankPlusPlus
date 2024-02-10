package dez.fortexx.bankplusplus.api.economy.result;

public sealed interface EconomyResult permits DescribedFailureEconomyResult, FailureEconomyResult, SuccessEconomyResult {
}
