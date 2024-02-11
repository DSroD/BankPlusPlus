package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.bank.IBankManager;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidCommandSenderResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BalanceCommand implements ICommand {
    private final IBankManager bankManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;

    public BalanceCommand(
            IBankManager bankManager,
            Localization localization,
            ICurrencyFormatter currencyFormatter
    ) {
        this.bankManager = bankManager;
        this.localization = localization;
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "balance";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getBalanceCommandDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of();
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of(
                "bankplusplus.use"
        );
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return handlePlayer(p, args);
        }
        return InvalidCommandSenderResult.instance;
    }

    private ICommandResult handlePlayer(Player p, String[] args) {
        final var balance = bankManager.getBalance(p);
        final var limit = bankManager.getLimit(p);

        final var component = new ComponentBuilder(localization.getBalance())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append(": ")
                .append(currencyFormatter.formatCurrency(balance))
                .append(".\n")
                .append(localization.getBalanceLimit())
                .color(ChatColor.AQUA).bold(false)
                .append(": ")
                .append(currencyFormatter.formatCurrency(limit.maximumMoney()))
                .append(".")
                .create();
        return new BaseComponentResult(
                component
        );
    }
}
