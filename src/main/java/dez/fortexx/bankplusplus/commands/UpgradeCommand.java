package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.bank.IBankLimitsManager;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.IArgumentsValidator;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidCommandSenderResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.ICurrencyFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class UpgradeCommand implements ICommand {
    private final Localization localization;
    private final IBankLimitsManager limitsManager;
    private final ICurrencyFormatter formatter;
    private final IArgumentsValidator argumentsValidator;

    public UpgradeCommand(
            IBankLimitsManager limitsManager,
            Localization localization,
            IArgumentsValidator argumentsValidator,
            ICurrencyFormatter formatter
    ) {
        this.localization = localization;
        this.limitsManager = limitsManager;
        this.formatter = formatter;
        this.argumentsValidator = argumentsValidator;
    }

    @Override
    public @NotNull String getCommandName() {
        return "upgrade";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getUpgrade();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of();
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.empty();
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return invokePlayer(p);
        }
        return InvalidCommandSenderResult.instance;
    }

    private @NotNull ICommandResult invokePlayer(Player p) {
        if (limitsManager.upgradeLimits(p)) {
            final var newLimit = limitsManager.getLimit(p);
            final var successComponent = new ComponentBuilder(localization.getUpgradeSuccessful())
                    .color(ChatColor.DARK_GREEN).bold(true)
                    .append("! ")
                    .append(localization.getNewBalanceLimitIs())
                    .bold(false)
                    .append(": ")
                    .append(formatter.formatCurrency(newLimit.maximumMoney()))
                    .color(ChatColor.GOLD)
                    .append(" (")
                    .color(ChatColor.DARK_GREEN)
                    .append(newLimit.name())
                    .append(").")
                    .create();
            return new BaseComponentResult(successComponent);
        }
        final var failedComponent = new ComponentBuilder(localization.getUpgradeFailed())
                .color(ChatColor.RED).bold(true)
                .create();
        return new BaseComponentResult(failedComponent);
    }
}
