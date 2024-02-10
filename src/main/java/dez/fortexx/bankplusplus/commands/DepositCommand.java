package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.bank.IBankBalanceManager;
import dez.fortexx.bankplusplus.bank.transaction.*;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.BigDecimalArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ErrorResult;
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

public class DepositCommand implements ICommand {
    private final BigDecimalArgument amountArgument;
    private final IBankBalanceManager transactionManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;

    public DepositCommand(
            IBankBalanceManager transactionManager,
            Localization localization,
            ICurrencyFormatter currencyFormatter
    ) {
        this.transactionManager = transactionManager;
        this.localization = localization;
        amountArgument = new BigDecimalArgument(localization.getAmount().toLowerCase());
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "deposit";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getDepositCommandDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of(
                amountArgument
        );
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of(
                "bank.use"
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

        final var amountString = args[0];

        final var amount = amountArgument.fromString(amountString);
        final var result = transactionManager.deposit(p, amount);

        if (result instanceof SuccessTransactionResult successRes) {
            return successResult(successRes);
        }
        if (result instanceof InsufficientFundsTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getInsufficientFunds() + ".");
        }
        if (result instanceof AmountTooSmallTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getAmountTooSmall() + ".");
        }
        if (result instanceof LimitsViolationsTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getLimitViolation() + ".");
        }
        if (result instanceof DescribedTransactionFailureResult dr) {
            return errorResult(dr.description());
        }
        return ErrorResult.instance;
    }

    @NotNull
    private BaseComponentResult successResult(SuccessTransactionResult sr) {
        final var component = new ComponentBuilder(localization.getDepositSuccessful())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append(". ")
                .append(localization.getNewBalance())
                .append(": ")
                .bold(false)
                .append(currencyFormatter.formatCurrency(sr.newBalance()))
                .color(ChatColor.GOLD).bold(true)
                .append(". ")
                .append(localization.getFees())
                .color(ChatColor.RED).bold(false)
                .append(": ")
                .append(currencyFormatter.formatCurrency(sr.feesPaid()))
                .append(".")
                .create();
        return new BaseComponentResult(
                component
        );
    }

    private static BaseComponentResult errorResult(String text) {
        final var component = new ComponentBuilder(text)
                .color(ChatColor.RED).bold(true)
                .create();
        return new BaseComponentResult(component);
    }
}
