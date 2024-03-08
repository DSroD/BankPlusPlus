package dez.fortexx.bankplusplus.commands.user;

import dez.fortexx.bankplusplus.bank.IBankLimitsManager;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidCommandSenderResult;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import dez.fortexx.bankplusplus.utils.formatting.IUpgradeRequirementFormatter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InfoCommand implements ICommand {
    private final IBankLimitsManager limitsManager;
    private final Localization localization;
    private final ICurrencyFormatter currencyFormatter;
    private final IUpgradeRequirementFormatter upgradeRequirementFormatter;

    public InfoCommand(
            IBankLimitsManager limitsManager,
            Localization localization, ICurrencyFormatter currencyFormatter, IUpgradeRequirementFormatter upgradeRequirementFormatter
    ) {
        this.limitsManager = limitsManager;
        this.localization = localization;
        this.currencyFormatter = currencyFormatter;
        this.upgradeRequirementFormatter = upgradeRequirementFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "info";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getInfoCommandDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of();
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus.use");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return handlePlayer(p);
        }
        return InvalidCommandSenderResult.instance;
    }

    private @NotNull ICommandResult handlePlayer(Player p) {
        final var bankLimit = limitsManager.getBankLevelLimit(p);
        final var nextLevelLimit = limitsManager.getNextBankLevelLimit(p);
        final var builder = new ComponentBuilder(localization.getAccountLevelName())
                .color(ChatColor.GOLD).bold(true)
                .append(": ")
                .append(bankLimit.name())
                .color(ChatColor.AQUA)
                .append("\n")
                .append(localization.getBalanceLimit())
                .color(ChatColor.GOLD)
                .append(": ")
                .append(currencyFormatter.formatCurrency(bankLimit.maximumMoney()))
                .color(ChatColor.AQUA);

        nextLevelLimit.ifPresent(nextLimit -> {
            final var reqs = nextLimit.upgradeRequirements()
                    .stream()
                    .map(upgradeRequirementFormatter::format)
                    .collect(Collectors.joining(", "));

            builder.append("\n")
                    .append(localization.getUpgradeAvailable())
                    .color(ChatColor.DARK_GREEN)
                    .append(":\n")
                    .append(localization.getNextLevel())
                    .color(ChatColor.GOLD).bold(false)
                    .append(": ")
                    .append(nextLimit.name())
                    .color(ChatColor.AQUA)
                    .append("\n")
                    .append(localization.getNextLevelBalanceLimit())
                    .color(ChatColor.GOLD)
                    .append(": ")
                    .append(currencyFormatter.formatCurrency(nextLimit.maximumMoney()))
                    .color(ChatColor.AQUA)
                    .append("\n")
                    .append(localization.getNextLevelRequirements())
                    .color(ChatColor.GOLD)
                    .append(": ")
                    .append(reqs)
                    .color(ChatColor.AQUA);
        });

        return new BaseComponentResult(builder.create());
    }
}
