package dez.fortexx.bankplusplus.placeholders;

import dez.fortexx.bankplusplus.BankPlusPlus;
import dez.fortexx.bankplusplus.bank.IBankEconomyManager;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BankPlusPlusPlaceholderExpansion extends PlaceholderExpansion {
    private final BankPlusPlus plugin;
    private final IBankEconomyManager bankEconomyManager;
    private final ICurrencyFormatter currencyFormatter;

    public BankPlusPlusPlaceholderExpansion(
            BankPlusPlus plugin,
            IBankEconomyManager bankEconomyManager,
            ICurrencyFormatter currencyFormatter
    ) {
        this.plugin = plugin;
        this.bankEconomyManager = bankEconomyManager;
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
            final var balance = bankEconomyManager.getBalance(player);
            return currencyFormatter.formatCurrency(balance);
        }

        if (params.equalsIgnoreCase("bank_level")) {
            final var level = bankEconomyManager.getBankLevelLimit(player);
            return level.name();
        }

        if (params.equalsIgnoreCase("bank_limit")) {
            final var level = bankEconomyManager.getBankLevelLimit(player);
            return currencyFormatter.formatCurrency(level.maximumMoney());
        }

        return null;
    }
}
