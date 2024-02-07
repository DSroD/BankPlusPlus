package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.api.economy.transaction.*;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.BigDecimalArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.IArgumentsValidator;
import dez.fortexx.bankplusplus.commands.api.result.*;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DepositCommand implements ICommand {
    private final BigDecimalArgument amountArgument;
    private final IBalanceManager transactionManager;
    private final Localization localization;
    private final IArgumentsValidator validator;
    private final ICurrencyFormatter currencyFormatter;

    public DepositCommand(
            IBalanceManager transactionManager,
            Localization localization,
            IArgumentsValidator validator,
            ICurrencyFormatter currencyFormatter
    ) {
        this.transactionManager = transactionManager;
        this.localization = localization;
        amountArgument = new BigDecimalArgument(localization.getAmount().toLowerCase());
        this.validator = validator;
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "deposit";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getDeposit();
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
                "bank.use.deposit"
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

        final var amountString = args[0];

        final var amount = amountArgument.fromString(amountString);
        final var result = transactionManager.deposit(p, amount);

        /*
        return switch (result) {
            case final SuccessTransactionResult successRes -> successResult(successRes);
            case InsufficientFundsTransactionResult insuffRes ->
                    errorResult(localization.getDepositFailed() + ". " + localization.getInsufficientFunds());
            case AmountTooSmallTransactionResult smallRes ->
                    errorResult(localization.getDepositFailed() + ". " + localization.getAmountTooSmall());
            case LimitsViolationsTransactionResult limViolRes ->
                    errorResult(localization.getDepositFailed() + ". " + localization.getLimitViolation());
            case DescribedTransactionFailureResult dr -> errorResult(dr.description());
        };
        */

        if (result instanceof SuccessTransactionResult successRes) {
            return successResult(successRes);
        }
        if (result instanceof InsufficientFundsTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getInsufficientFunds());
        }
        if (result instanceof AmountTooSmallTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getAmountTooSmall());
        }
        if (result instanceof LimitsViolationsTransactionResult) {
            return errorResult(localization.getDepositFailed() + ". " + localization.getLimitViolation());
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
