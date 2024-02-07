package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.api.economy.transaction.*;
import dez.fortexx.bankplusplus.bank.fees.IFeeProvider;
import dez.fortexx.bankplusplus.bank.levels.BankLimit;
import dez.fortexx.bankplusplus.configuration.PluginConfiguration;
import dez.fortexx.bankplusplus.events.IEventCaller;
import dez.fortexx.bankplusplus.events.PlayerBankTransactionEvent;
import dez.fortexx.bankplusplus.events.PlayerBankUpgradeEvent;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

//TODO: unit tests
public class BankManager implements IBankLimitsManager, IBalanceManager {
    @Unmodifiable
    private final List<BankLimit> bankLimits;
    private final IBalanceManager playerBalanceManager;
    private final IBankStore bankStore;
    private final IFeeProvider feeProvider;
    private final IEventCaller eventCaller;
    private final PluginConfiguration configuration;

    public BankManager(
            Function<IBalanceManager, @Unmodifiable List<BankLimit>> bankLevelSupplier,
            IBalanceManager playerBalanceManager,
            IBankStore bankStore, IFeeProvider feeProvider,
            IEventCaller eventCaller,
            PluginConfiguration configuration
    ) {
        this.bankLimits = bankLevelSupplier.apply(this);
        this.playerBalanceManager = playerBalanceManager;
        this.bankStore = bankStore;
        this.feeProvider = feeProvider;
        this.eventCaller = eventCaller;
        this.configuration = configuration;
    }

    @Override
    public boolean upgradeLimits(Player player) {
        // Level nubmer is level index + 1
        final var currentLevelNumber = bankStore.getBankLevel(player.getUniqueId());
        // Can not upgrade
        if (currentLevelNumber >= bankLimits.size())
            return false;

        final var bankUpgradeRequirements = bankLimits.get(currentLevelNumber) // This is the next level in the array
                .upgradeRequirements();

        for (final var requirement : bankUpgradeRequirements) {
            // Only check before taking
            if (!requirement.has(player)) {
                return false;
            }
        }

        for (final var requirement : bankUpgradeRequirements) {
            // Requirements OK - take
            if (!requirement.takeFrom(player))
                // If unable to take the requirement - cancel all - already taken stuff is deleted
                // TODO: transactional approach
                return false;
        }

        final var newLevel = currentLevelNumber + 1;
        bankStore.upgradeLevel(player.getUniqueId());
        eventCaller.call(new PlayerBankUpgradeEvent(player, newLevel));
        return true;
    }

    @Override
    public BankLimit getLimit(Player player) {
        final var levelIdx = bankStore.getBankLevel(player.getUniqueId()) - 1;
        return bankLimits.get(levelIdx);
    }

    @Override
    public ITransactionResult deposit(OfflinePlayer player, BigDecimal amount) {
        final var playerUUID = player.getUniqueId();
        final var currentBankBalance = bankStore.getBankFunds(playerUUID);

        final var roundedAmount = amount.setScale(configuration.getDecimalPrecision(), RoundingMode.CEILING);

        final var currentPlayerBalance = playerBalanceManager.getBalance(player);

        if (currentPlayerBalance.compareTo(roundedAmount) < 0) {
            return InsufficientFundsTransactionResult.instance;
        }

        final var fee = feeProvider.getDepositFee(player, roundedAmount);
        final var depositedAmount = roundedAmount.subtract(fee);

        if (depositedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return AmountTooSmallTransactionResult.instance;

        var bankLevel = bankStore.getBankLevel(playerUUID);
        if (bankLevel >= bankLimits.size()) {
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
        if (!(takeFromPlayerResult instanceof SuccessTransactionResult)) {
            return takeFromPlayerResult;
        }

        bankStore.addBankFunds(playerUUID, depositedAmount);
        eventCaller.call(
                new PlayerBankTransactionEvent(player, depositedAmount, fee, TransactionType.DEPOSIT)
        );
        return new SuccessTransactionResult(
                finalBalance,
                fee
        );
    }

    @Override
    public ITransactionResult withdraw(OfflinePlayer player, BigDecimal amount) {
        final var playerUUID = player.getUniqueId();
        final var currentBankBalance = bankStore.getBankFunds(playerUUID);

        final var roundedAmount = amount.setScale(configuration.getDecimalPrecision(), RoundingMode.CEILING);

        if (roundedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return AmountTooSmallTransactionResult.instance;

        final var fee = feeProvider.getWithdrawFee(player, roundedAmount);
        final var takenAmount = roundedAmount.add(fee);

        if (currentBankBalance.compareTo(takenAmount) < 0) {
            return InsufficientFundsTransactionResult.instance;
        }

        // TODO: transactions
        if (!bankStore.takeBankFunds(playerUUID, takenAmount)) {
            // TODO: use different message-independent class
            return new DescribedTransactionFailureResult("Failed to take money from the bank.");
        }
        playerBalanceManager.deposit(player, roundedAmount);
        eventCaller.call(
                new PlayerBankTransactionEvent(player, takenAmount, fee, TransactionType.WITHDRAW)
        );

        return new SuccessTransactionResult(
                currentBankBalance.subtract(amount),
                fee
        );
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer p) {
        return bankStore.getBankFunds(p.getUniqueId());
    }
}
