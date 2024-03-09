package dez.fortexx.bankplusplus.api.economy.result;

public sealed interface EconomyResult permits AmountTooSmall, DescribedFailure, Failure, InsufficientFunds, LimitViolation, Success {
}
