package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.DescribedFailureEconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.FailureEconomyResult;
import dez.fortexx.bankplusplus.bank.balance.IBankEconomyManager;
import dez.fortexx.bankplusplus.bank.fees.IFeeProvider;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.transaction.*;
import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.IUpgradePermissionManager;
import dez.fortexx.bankplusplus.bank.upgrade.result.*;
import dez.fortexx.bankplusplus.events.IEventDispatcher;
import dez.fortexx.bankplusplus.events.PlayerBankTransactionEvent;
import dez.fortexx.bankplusplus.events.PlayerBankUpgradeEvent;
import dez.fortexx.bankplusplus.logging.ILogger;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * Provides transactions between bank account any player account provided using playerBalanceManager
 */
public class BankManager implements IBankManager {
    @Unmodifiable
    private final List<BankLimit> bankLimits;
    private final IEconomyManager playerBalanceManager;
    private final IBankEconomyManager bankEconomyManager;
    private final IFeeProvider feeProvider;
    private final IEventDispatcher eventDispatcher;
    private final IUpgradePermissionManager upgradePermissionChecker;
    private final ITransactionRounding rounding;
    private final ILogger logger;

    public BankManager(
            @Unmodifiable List<BankLimit> limits,
            IEconomyManager playerBalanceManager,
            IBankEconomyManager bankEconomyManager,
            IUpgradePermissionManager upgradePermissionChecker,
            IFeeProvider feeProvider,
            IEventDispatcher eventDispatcher,
            ITransactionRounding rounding, ILogger logger
    ) {
        this.bankLimits = limits;
        this.playerBalanceManager = playerBalanceManager;
        this.bankEconomyManager = bankEconomyManager;
        this.feeProvider = feeProvider;
        this.eventDispatcher = eventDispatcher;
        this.rounding = rounding;
        this.upgradePermissionChecker = upgradePermissionChecker;
        this.logger = logger;
    }

    @Override
    public IUpgradeResult upgradeLimits(Player player) {
        // Level nubmer is level index + 1
        final var currentLevelNumber = bankEconomyManager.getBankLevel(player);
        // Can not upgrade
        if (currentLevelNumber >= bankLimits.size())
            return MaxLevelUpgradeResult.instance;

        // currentLevelNumber is actually index of the next level!
        final var nextLevel = bankLimits.get(currentLevelNumber);

        if (!upgradePermissionChecker.canUpgrade(player, nextLevel)) {
            return MissingPermissionsUpgradeResult.instance;
        }

        final var bankUpgradeRequirements = nextLevel
                .upgradeRequirements();

        final var missingRequirements = new LinkedList<IUpgradeRequirement>();
        for (final var requirement : bankUpgradeRequirements) {
            // Only check before taking
            if (!requirement.has(player)) {
                missingRequirements.add(requirement);
            }
        }

        if (!missingRequirements.isEmpty()) {
            return new MissingRequirementsUpgradeResult(missingRequirements);
        }

        for (final var requirement : bankUpgradeRequirements) {
            // Requirements OK - take
            if (!requirement.takeFrom(player))
                // TODO: transactional approach
                // If unable to take the requirement - cancel all - already taken stuff is gone
                return new MissingRequirementsUpgradeResult(List.of(requirement));
        }

        final var newLevel = currentLevelNumber + 1;
        bankEconomyManager.upgradeLevel(player);

        eventDispatcher.dispatch(new PlayerBankUpgradeEvent(player, newLevel));
        logger.info(
                () -> "[Upgrade] " + player.getName() + " upgraded bank to level " + newLevel
        );

        return SuccessUpgradeResult.instance;
    }

    @Override
    public BankLimit getLimit(Player player) {
        final var levelIdx = bankEconomyManager.getBankLevel(player) - 1;
        return bankLimits.get(levelIdx);
    }

    @Override
    public Optional<BankLimit> getNextLevelLimit(Player player) {
        final var nextLevelIdx = bankEconomyManager.getBankLevel(player);
        if (nextLevelIdx >= bankLimits.size()) { // We are at the maximum level
            return Optional.empty();
        }

        final var nextBankLimit = bankLimits.get(nextLevelIdx);

        if (!upgradePermissionChecker.canUpgrade(player, nextBankLimit)) {
            return Optional.empty();
        }

        return Optional.of(nextBankLimit);
    }

    @Override
    public ITransactionResult deposit(Player player, BigDecimal amount) {
        final var currentBankBalance = bankEconomyManager.getBalance(player);

        final var roundedAmount = rounding.round(amount);

        final var currentPlayerBalance = playerBalanceManager.getBalance(player);

        if (currentPlayerBalance.compareTo(roundedAmount) < 0) {
            return new InsufficientFundsTransactionResult(currentPlayerBalance);
        }

        final var fee = feeProvider.getDepositFee(player, roundedAmount);
        final var depositedAmount = roundedAmount.subtract(fee);

        if (depositedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return AmountTooSmallTransactionResult.instance;

        var bankLevel = bankEconomyManager.getBankLevel(player);
        if (bankLevel > bankLimits.size()) {
            // TODO: handle fallback somehow better
            bankLevel = 1;
        }
        final var bankIdx = bankLevel - 1;
        final var bankLimit = bankLimits.get(bankIdx).maximumMoney();
        final var finalBalance = currentBankBalance.add(depositedAmount);
        if (finalBalance.compareTo(bankLimit) > 0) {
            return LimitsViolationsTransactionResult.instance;
        }

        // TODO: transactions
        final var takeFromPlayerResult = playerBalanceManager.withdraw(player, roundedAmount);
        if (takeFromPlayerResult instanceof DescribedFailureEconomyResult e) {
            return new DescribedTransactionFailureResult(e.description());
        }
        if (takeFromPlayerResult instanceof FailureEconomyResult) {
            // TODO: better representation
            return new DescribedTransactionFailureResult("Failure removing money from player");
        }

        bankEconomyManager.deposit(player, depositedAmount);


        eventDispatcher.dispatch(
                new PlayerBankTransactionEvent(player, depositedAmount, fee, TransactionType.DEPOSIT)
        );

        logger.info(
                () -> "[Deposit] " + player.getName() + " deposited " + depositedAmount.toPlainString() + " to the bank + paid fee of " + fee.toPlainString()
        );

        return new SuccessTransactionResult(
                finalBalance,
                fee
        );
    }

    @Override
    public ITransactionResult withdraw(Player player, BigDecimal amount) {
        final var currentBankBalance = bankEconomyManager.getBalance(player);

        final var roundedAmount = rounding.round(amount);

        if (roundedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return AmountTooSmallTransactionResult.instance;

        final var fee = feeProvider.getWithdrawFee(player, roundedAmount);
        final var takenAmount = roundedAmount.add(fee);

        if (currentBankBalance.compareTo(takenAmount) < 0) {
            final var maximalPossibleWithdraw = feeProvider.getMaximalWithdraw(player, currentBankBalance);
            return new InsufficientFundsTransactionResult(maximalPossibleWithdraw);
        }

        // TODO: transactions
        final var withdrawResult = bankEconomyManager.withdraw(player, takenAmount);

        if (withdrawResult instanceof FailureEconomyResult) {
            // TODO: use different message-independent class
            return new DescribedTransactionFailureResult("Failed to take money from the bank.");
        }
        if (withdrawResult instanceof DescribedFailureEconomyResult e) {
            return new DescribedTransactionFailureResult(e.description());
        }

        playerBalanceManager.deposit(player, roundedAmount);

        eventDispatcher.dispatch(
                new PlayerBankTransactionEvent(player, roundedAmount, fee, TransactionType.WITHDRAW)
        );

        logger.info(
                () -> "[Withdraw] " + player.getName() + " withdrawn " + roundedAmount.toPlainString() + " from the bank + paid fee of " + fee.toPlainString()
        );

        return new SuccessTransactionResult(
                currentBankBalance.subtract(takenAmount),
                fee
        );
    }

    @Override
    public BigDecimal getBalance(Player p) {
        return bankEconomyManager.getBalance(p);
    }
}
