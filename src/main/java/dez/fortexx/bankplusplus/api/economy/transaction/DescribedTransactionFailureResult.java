package dez.fortexx.bankplusplus.api.economy.transaction;

public record DescribedTransactionFailureResult(
        String description
) implements ITransactionResult {
}
