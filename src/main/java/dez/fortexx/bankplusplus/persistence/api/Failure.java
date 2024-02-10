package dez.fortexx.bankplusplus.persistence.api;

public record Failure(
        String message
) implements PersistenceResult {
}
