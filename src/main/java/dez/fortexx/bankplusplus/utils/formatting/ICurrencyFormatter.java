package dez.fortexx.bankplusplus.utils.formatting;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public interface ICurrencyFormatter {
    @NotNull
    String formatCurrency(@NotNull BigDecimal amount);
}
