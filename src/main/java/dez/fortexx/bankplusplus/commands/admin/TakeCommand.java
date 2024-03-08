package dez.fortexx.bankplusplus.commands.admin;

import dez.fortexx.bankplusplus.api.economy.result.AmountTooSmall;
import dez.fortexx.bankplusplus.api.economy.result.DescribedFailure;
import dez.fortexx.bankplusplus.api.economy.result.InsufficientFunds;
import dez.fortexx.bankplusplus.api.economy.result.Success;
import dez.fortexx.bankplusplus.bank.balance.IBankEconomyManager;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.BigDecimalArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.OfflinePlayerArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ErrorResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.ITimeProvider;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class TakeCommand implements ICommand {
    private final IBankEconomyManager bankEconomyManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;

    private final OfflinePlayerArgument playerArgument;
    private final BigDecimalArgument amountArgument;

    public TakeCommand(
            Plugin plugin,
            IBankEconomyManager bankEconomyManager,
            Localization localization,
            ITimeProvider timeProvider,
            ICurrencyFormatter currencyFormatter
    ) {
        this.bankEconomyManager = bankEconomyManager;
        this.localization = localization;
        this.currencyFormatter = currencyFormatter;
        playerArgument = new OfflinePlayerArgument(
                localization.getPlayer().toLowerCase(),
                plugin,
                timeProvider
        );
        amountArgument = new BigDecimalArgument(
                localization.getAmount().toLowerCase()
        );
    }

    @Override
    public @NotNull String getCommandName() {
        return "take";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getTakeDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of(
                playerArgument,
                amountArgument
        );
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus.admin.take");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        final var player = playerArgument.fromString(args[0]);
        final var amount = amountArgument.fromString(args[1]);

        if (player == null) {
            return errorResult(localization.getCommandBase().getPlayerNotFound());
        }

        final var result = bankEconomyManager.withdraw(player, amount);

        if (result instanceof Success sr) {
            return successResult(sr, player.getName());
        }

        if (result instanceof InsufficientFunds) {
            return errorResult(
                    localization.getCommandAdmin().getTakeFailed() + " " + player.getName() + ". " +
                    localization.getLimitViolation() + "."
            );
        }

        if (result instanceof AmountTooSmall) {
            return errorResult(
                    localization.getCommandAdmin().getTakeFailed() + " " + player.getName() + ". "
                    + localization.getAmountTooSmall() + "."
            );
        }

        if (result instanceof DescribedFailure dr) {
            return errorResult(dr.description());
        }
        // Limit violation should not happen
        return ErrorResult.instance;
    }

    private BaseComponentResult errorResult(String message) {
        final var component = new ComponentBuilder(message)
                .color(ChatColor.RED).bold(true)
                .create();
        return new BaseComponentResult(component);
    }

    private BaseComponentResult successResult(Success result, String name) {
        final var component = new ComponentBuilder((localization.getCommandAdmin().getTakeSuccessful()))
                .color(ChatColor.DARK_GREEN).bold(true)
                .append(" ")
                .append(name)
                .color(ChatColor.GOLD)
                .append(". ")
                .color(ChatColor.DARK_GREEN)
                .append(localization.getNewBalance())
                .bold(false)
                .append(": ")
                .append(currencyFormatter.formatCurrency(result.newBalance()))
                .color(ChatColor.GOLD).bold(true)
                .append(".")
                .color(ChatColor.DARK_GREEN).bold(false)
                .create();
        return new BaseComponentResult(component);
    }
}
