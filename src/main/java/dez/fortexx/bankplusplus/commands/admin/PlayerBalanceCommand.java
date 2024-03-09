package dez.fortexx.bankplusplus.commands.admin;

import dez.fortexx.bankplusplus.bank.IBankEconomyManager;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.OfflinePlayerArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PlayerBalanceCommand implements ICommand {
    private final OfflinePlayerArgument playerArgument;
    private final IBankEconomyManager bankEconomyManager;
    private final ICurrencyFormatter currencyFormatter;
    private final Localization localization;

    public PlayerBalanceCommand(
            IBankEconomyManager bankEconomyManager,
            Localization localization,
            ICurrencyFormatter currencyFormatter,
            OfflinePlayerArgument playerArgument
    ) {
        this.playerArgument = playerArgument;
        this.bankEconomyManager = bankEconomyManager;
        this.currencyFormatter = currencyFormatter;
        this.localization = localization;
    }

    @Override
    public @NotNull String getCommandName() {
        return "pbalance";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getPbalanceDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of(
                playerArgument
        );
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus.admin.playerbalance");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        final var playerName = args[0];
        final var offlinePlayer = playerArgument.fromString(playerName);

        if (offlinePlayer == null) {
            return errorResult(localization.getCommandBase().getPlayerNotFound());
        }

        final var balanceResult = bankEconomyManager.getBalance(offlinePlayer);

        final var component = new ComponentBuilder(localization.getBalance())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append("(")
                .append(playerName)
                .append("): ")
                .append(currencyFormatter.formatCurrency(balanceResult))
                .color(ChatColor.GOLD)
                .append(".")
                .color(ChatColor.DARK_GREEN)
                .create();

        return new BaseComponentResult(component);

    }

    private BaseComponentResult errorResult(String message) {
        final var component = new ComponentBuilder(message)
                .color(ChatColor.RED).bold(true)
                .create();
        return new BaseComponentResult(component);
    }
}
