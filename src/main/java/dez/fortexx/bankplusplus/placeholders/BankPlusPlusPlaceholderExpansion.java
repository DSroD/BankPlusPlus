package dez.fortexx.bankplusplus.placeholders;

import dez.fortexx.bankplusplus.BankPlusPlus;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BankPlusPlusPlaceholderExpansion extends PlaceholderExpansion {
    private final BankPlusPlus plugin;
    private final IBankStore bankStore;
    private final ICurrencyFormatter currencyFormatter;

    public BankPlusPlusPlaceholderExpansion(
            BankPlusPlus plugin,
            IBankStore bankStore,
            ICurrencyFormatter currencyFormatter
    ) {
        this.plugin = plugin;
        this.bankStore = bankStore;
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bankplusplus";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("bank_balance")) {
            final var balance = bankStore.getBankFunds(player.getUniqueId());
            return currencyFormatter.formatCurrency(balance);
        }

        if (params.equalsIgnoreCase("bank_level")) {
            final var level = bankStore.getBankLevel(player.getUniqueId());
            return Integer.toString(level);
        }

        return null;
    }
}
