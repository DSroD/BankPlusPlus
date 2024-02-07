package dez.fortexx.bankplusplus.persistence.utils;

public record Failure(
        String message
) implements PersistenceResult {
}
