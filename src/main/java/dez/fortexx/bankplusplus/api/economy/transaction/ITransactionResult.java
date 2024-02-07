package dez.fortexx.bankplusplus.api.economy.transaction;

public sealed interface ITransactionResult permits AmountTooSmallTransactionResult, DescribedTransactionFailureResult, InsufficientFundsTransactionResult, LimitsViolationsTransactionResult, SuccessTransactionResult {

}
