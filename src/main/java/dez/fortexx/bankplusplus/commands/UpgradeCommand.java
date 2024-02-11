package dez.fortexx.bankplusplus.commands;

import dez.fortexx.bankplusplus.bank.IBankLimitsManager;
import dez.fortexx.bankplusplus.bank.upgrade.result.MaxLevelUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingPermissionsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingRequirementsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.SuccessUpgradeResult;
import dez.fortexx.bankplusplus.commands.api.ICommand;
import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.result.BaseComponentResult;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import dez.fortexx.bankplusplus.commands.api.result.InvalidCommandSenderResult;
import dez.fortexx.bankplusplus.commands.api.result.MissingPermissionsResult;
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

public class UpgradeCommand implements ICommand {
    private final Localization localization;
    private final IBankLimitsManager limitsManager;
    private final ICurrencyFormatter currencyFormatter;
    private final IUpgradeRequirementFormatter upgradeRequirementFormatter;

    public UpgradeCommand(
            IBankLimitsManager limitsManager,
            Localization localization,
            ICurrencyFormatter currencyFormatter,
            IUpgradeRequirementFormatter upgradeRequirementFormatter
    ) {
        this.localization = localization;
        this.limitsManager = limitsManager;
        this.currencyFormatter = currencyFormatter;
        this.upgradeRequirementFormatter = upgradeRequirementFormatter;
    }

    @Override
    public @NotNull String getCommandName() {
        return "upgrade";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return localization.getCommandDescriptions().getUpgradeCommandDescription();
    }

    @Override
    public @NotNull List<ICommandArgument<?>> getCommandArguments() {
        return List.of();
    }

    @Override
    public @NotNull Optional<String> getPermission() {
        return Optional.of("bankplusplus");
    }

    @Override
    public @NotNull ICommandResult invoke(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            return handlePlayer(p);
        }
        return InvalidCommandSenderResult.instance;
    }

    private @NotNull ICommandResult handlePlayer(Player p) {
        final var result = limitsManager.upgradeLimits(p);
        if (result instanceof MissingPermissionsUpgradeResult) {
            return MissingPermissionsResult.instance;
        }
        if (result instanceof MissingRequirementsUpgradeResult r) {
            return handleMissingRequirements(r);
        }
        if (result instanceof SuccessUpgradeResult) {
            return handleSuccess(p);
        }
        if (result instanceof MaxLevelUpgradeResult) {
            return handleMaxLevel();
        }


        // More sad noises for missing exhaustive pattern matching
        final var failedComponent = new ComponentBuilder(localization.getUpgradeFailed())
                .color(ChatColor.RED).bold(true)
                .create();
        return new BaseComponentResult(failedComponent);
    }

    private ICommandResult handleSuccess(Player p) {
        final var newLimit = limitsManager.getLimit(p);
        final var component = new ComponentBuilder(localization.getUpgradeSuccessful())
                .color(ChatColor.DARK_GREEN).bold(true)
                .append("! ")
                .append(localization.getNewBalanceLimitIs())
                .bold(false)
                .append(": ")
                .append(currencyFormatter.formatCurrency(newLimit.maximumMoney()))
                .color(ChatColor.GOLD)
                .append(" (")
                .color(ChatColor.DARK_GREEN)
                .append(newLimit.name())
                .append(").")
                .create();
        return new BaseComponentResult(component);
    }

    private ICommandResult handleMaxLevel() {
        final var component = new ComponentBuilder(localization.getUpgradeFailed())
                .color(ChatColor.RED).bold(true)
                .append(". ")
                .append(localization.getBankAlreadyMaxLevel())
                .bold(false)
                .append(".")
                .create();
        return new BaseComponentResult(component);
    }

    private ICommandResult handleMissingRequirements(MissingRequirementsUpgradeResult r) {
        final var missingRequirementsLines = r.missingRequirements()
                .stream()
                .map(upgradeRequirementFormatter::format)
                .collect(Collectors.joining(", "));

        final var component = new ComponentBuilder(localization.getUpgradeFailed())
                .color(ChatColor.RED).bold(true)
                .append(". ")
                .append(localization.getMissingRequirements())
                .bold(false)
                .append(": ")
                .append(missingRequirementsLines)
                .append(".").create();

        return new BaseComponentResult(component);
    }
}
