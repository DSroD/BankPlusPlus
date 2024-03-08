package dez.fortexx.bankplusplus.bank.balance;

import dez.fortexx.bankplusplus.api.economy.result.*;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.IUpgradePermissionManager;
import dez.fortexx.bankplusplus.bank.upgrade.result.*;
import dez.fortexx.bankplusplus.events.IEventDispatcher;
import dez.fortexx.bankplusplus.events.PlayerBankUpgradeEvent;
import dez.fortexx.bankplusplus.logging.ILogger;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * EconomyManager proxy over bank store
 */
public class BankEconomyManager implements IBankEconomyManager {
    private final IBankStore bankStore;
    @Unmodifiable
    private List<BankLimit> bankLimits;
    private final IUpgradePermissionManager upgradePermissionManager;
    private final IEventDispatcher eventDispatcher;
    private final ILogger logger;

    public BankEconomyManager(
            IBankStore bankStore,
            IUpgradePermissionManager upgradePermissionManager,
            IEventDispatcher eventDispatcher,
            ILogger logger
    ) {
        this.bankStore = bankStore;
        this.upgradePermissionManager = upgradePermissionManager;
        this.eventDispatcher = eventDispatcher;
        this.logger = logger;
    }

    public void setBankLimits(@Unmodifiable List<BankLimit> limits) {
        this.bankLimits = limits;
    }

    @Override
    public EconomyResult deposit(OfflinePlayer player, BigDecimal amount) {
        // TODO: minimal transaction size config
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return AmountTooSmall.instance;
        }

        final var playerUUID = player.getUniqueId();
        final var bankLevel = bankStore.getBankLevel(playerUUID);
        final var limit = bankLimits.get(bankLevel - 1); // index is level - 1
        final var maxBalance = limit.maximumMoney();
        final var currentBalance = bankStore.getBankFunds(playerUUID);
        final var balanceAfterDeposit = currentBalance.add(amount);
        if (maxBalance.compareTo(balanceAfterDeposit) < 0) {
            return LimitViolation.instance;
        }
        if (bankStore.addBankFunds(player.getUniqueId(), amount))
            return new Success(balanceAfterDeposit, BigDecimal.ZERO);
        return Failure.instance;
    }

    @Override
    public EconomyResult withdraw(OfflinePlayer player, BigDecimal amount) {
        // TODO: minimal transaction size config
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return AmountTooSmall.instance;
        }
        final var playerUUID = player.getUniqueId();
        final var currentBalance = bankStore.getBankFunds(playerUUID);
        if (currentBalance.compareTo(amount) < 0) {
            return new InsufficientFunds(currentBalance);
        }
        if (bankStore.takeBankFunds(player.getUniqueId(), amount))
            return new Success(currentBalance.subtract(amount), BigDecimal.ZERO);
        return Failure.instance;
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return bankStore.getBankFunds(player.getUniqueId());
    }

    @Override
    public IUpgradeResult upgradeLimits(Player player) {
        final var playerUUID = player.getUniqueId();
        final var currentLevelNumber = bankStore.getBankLevel(playerUUID);
        if (currentLevelNumber >= bankLimits.size()) {
            return MaxLevelUpgradeResult.instance;
        }

        final var nextLevel = bankLimits.get(currentLevelNumber);

        if (!upgradePermissionManager.canUpgrade(player, nextLevel)) {
            return MissingPermissionsUpgradeResult.instance;
        }

        final var requirements = nextLevel.upgradeRequirements();

        final var missingRequirements = new LinkedList<IUpgradeRequirement>();
        for (final var requirement : requirements) {
            // Only check before taking
            if (!requirement.has(player)) {
                missingRequirements.add(requirement);
            }
        }

        if (!missingRequirements.isEmpty()) {
            return new MissingRequirementsUpgradeResult(missingRequirements);
        }

        for (final var requirement : requirements) {
            if (!requirement.takeFrom(player)) {
                return new MissingRequirementsUpgradeResult(List.of(requirement));
            }
        }

        final var newLevel = currentLevelNumber + 1;
        bankStore.upgradeLevel(player.getUniqueId());

        eventDispatcher.dispatch(new PlayerBankUpgradeEvent(player, newLevel));
        logger.info(
                () -> "[Upgrade] " + player.getName() + " upgraded bank to level " + newLevel
        );

        return SuccessUpgradeResult.instance;
    }

    @Override
    public BankLimit getBankLevelLimit(OfflinePlayer player) {
        final var level = bankStore.getBankLevel(player.getUniqueId());
        final var idx = level - 1;
        return bankLimits.get(idx);
    }

    @Override
    public Optional<BankLimit> getNextBankLevelLimit(OfflinePlayer player) {
        final var nextLevelIdx = bankStore.getBankLevel(player.getUniqueId());
        return Optional.of(nextLevelIdx)
                .filter(x -> x < bankLimits.size())
                .map(bankLimits::get);
    }
}
