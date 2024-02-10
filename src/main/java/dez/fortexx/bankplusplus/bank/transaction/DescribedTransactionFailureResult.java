package dez.fortexx.bankplusplus.bank.transaction;

public record DescribedTransactionFailureResult(
        String description
) implements ITransactionResult {
}
