package dez.fortexx.bankplusplus.commands.user;

import dez.fortexx.bankplusplus.api.economy.result.*;
import dez.fortexx.bankplusplus.bank.IBankTransactionManager;
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

public class WithdrawCommand implements ICommand {
    private final BigDecimalArgument amountArgument;
    private final IBankTransactionManager transactionManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;

    public WithdrawCommand(
            IBankTransactionManager transactionManager,
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
        return "withdraw";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getWithdrawCommandDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of(
                amountArgument
        );
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus.use");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return dispatchPlayer(p, args);
        }

        else return InvalidCommandSenderResult.instance;
    }

    private ICommandResult dispatchPlayer(Player p, String[] args) {
        final var amountString = args[0];

        final var amount = amountArgument.fromString(amountString);
        final var result = transactionManager.withdrawFromBank(p, amount);

        if (result instanceof Success successRes) {
            return successResult(successRes);
        }
        if (result instanceof InsufficientFunds i) {
            return insufficientFundsResult(i);
        }
        if (result instanceof AmountTooSmall) {
            return errorResult(localization.getWithdrawFailed() + ". " + localization.getAmountTooSmall());
        }
        if (result instanceof LimitViolation) {
            return errorResult(localization.getWithdrawFailed() + ". " + localization.getLimitViolation());
        }
        if (result instanceof DescribedFailure dr) {
            return errorResult(dr.description());
        }
        return ErrorResult.instance;
    }

    @NotNull
    private BaseComponentResult successResult(Success sr) {
        final var component = new ComponentBuilder(localization.getWithdrawSuccessful())
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

    private BaseComponentResult insufficientFundsResult(InsufficientFunds res) {
        final var component = new ComponentBuilder(localization.getWithdrawFailed())
                .color(ChatColor.RED).bold(true)
                .append(". ")
                .append(localization.getInsufficientFunds())
                .append(". ")
                .append(localization.getMaximumWithdrawalPossible())
                .append(": ")
                .append(currencyFormatter.formatCurrency(res.maximalTransactionAmountAllowed()))
                .create();

        return new BaseComponentResult(component);
    }
}
