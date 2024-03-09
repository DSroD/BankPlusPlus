package dez.fortexx.bankplusplus.commands.admin;

import dez.fortexx.bankplusplus.bank.IBankEconomyManager;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.OfflinePlayerArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PlayerDowngradeCommand implements ICommand {
    private final IBankEconomyManager bankEconomyManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;

    private final OfflinePlayerArgument playerArgument;

    public PlayerDowngradeCommand(
            IBankEconomyManager bankEconomyManager,
            Localization localization,
            ICurrencyFormatter currencyFormatter,
            OfflinePlayerArgument playerArgument
    ) {
        this.bankEconomyManager = bankEconomyManager;
        this.localization = localization;
        this.currencyFormatter = currencyFormatter;
        this.playerArgument = playerArgument;
    }

    @Override
    public @NotNull String getCommandName() {
        return "pdowngrade";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getPdowngradeDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of(
                playerArgument
        );
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus.admin.downgrade");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        final var player = playerArgument.fromString(args[0]);

        if (player == null) {
            return errorResult(localization.getCommandBase().getPlayerNotFound());
        }

        final var newLimit = bankEconomyManager.forceDowngradeLimit(player);

        return newLimit.map(
                (limit) -> successResult(player, limit)
        ).orElseGet(
                () -> errorResult(
                        localization.getCommandAdmin().getDowngradeFailed() + " "
                                + player.getName() + ". " + localization.getCommandAdmin().getBankLevelMinimal() + "."
                )
        );
    }

    private BaseComponentResult successResult(OfflinePlayer player, BankLimit newLimit) {
        final var component = new ComponentBuilder(localization.getCommandAdmin().getDowngradeSuccessful())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append(" ")
                .append(player.getName())
                .append(". ")
                .append("New level: ")
                .bold(false)
                .append(newLimit.name())
                .color(ChatColor.GOLD)
                .append(" (")
                .color(ChatColor.GREEN)
                .append(currencyFormatter.formatCurrency(newLimit.maximumMoney()))
                .append(").")
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
