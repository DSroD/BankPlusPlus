package dez.fortexx.bankplusplus.utils.formatting;

import dez.fortexx.bankplusplus.localization.Localization;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class CurrencyFormatter implements ICurrencyFormatter {
    private final Localization localization;

    public CurrencyFormatter(Localization localization) {
        this.localization = localization;
    }

    @Override
    public @NotNull String formatCurrency(@NotNull BigDecimal amount) {
        final var amountString = amount.toPlainString();
        final var currencySymbol = localization.getCurrencySymbol();
        return localization.isCurrencySymbolBeforeNumber()
                ? currencySymbol + amountString
                : amountString + currencySymbol;
    }
}
