package dez.fortexx.bankplusplus.utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public interface ICurrencyFormatter {
    @NotNull
    String formatCurrency(@NotNull BigDecimal amount);
}
