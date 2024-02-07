package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.api.economy.transaction.ITransactionResult;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.IArgumentsValidator;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidCommandSenderResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidUsageResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BalanceCommand implements ICommand {
    private final IBalanceManager balanceManager;
    private final Localization localization;
    private final IArgumentsValidator validator;
    private final ICurrencyFormatter currencyFormatter;

    public BalanceCommand(
            IBalanceManager balanceManager,
            Localization localization,
            IArgumentsValidator validator,
            ICurrencyFormatter currencyFormatter
    ) {
        this.balanceManager = balanceManager;
        this.localization = localization;
        this.validator = validator;
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "balance";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getBalance();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of();
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of(
                "bank.use.balance"
        );
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return dispatchPlayer(p, args);
        }
        return InvalidCommandSenderResult.instance;
    }

    private ICommandResult dispatchPlayer(Player p, String[] args) {
        if (!validator.validate(getCommandArguments(), args))
            return InvalidUsageResult.instance;

        final var balance = balanceManager.getBalance(p);

        final var component = new ComponentBuilder(localization.getBalance())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append(": ")
                .append(currencyFormatter.formatCurrency(balance))
                .append(".")
                .create();
        return new BaseComponentResult(
                component
        );
    }
}
