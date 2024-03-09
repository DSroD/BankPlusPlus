package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.*;
import dez.fortexx.bankplusplus.bank.fees.IFeeProvider;
import dez.fortexx.bankplusplus.events.IEventDispatcher;
import dez.fortexx.bankplusplus.events.PlayerBankTransactionEvent;
import dez.fortexx.bankplusplus.logging.ILogger;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.entity.Player;

import java.math.BigDecimal;


/**
 * Provides transactions between bank account any player account provided using playerBalanceManager
 */
public final class BankTransactionManager implements dez.fortexx.bankplusplus.bank.IBankTransactionManager {
    private final IEconomyManager otherEconomyManager;
    private final IBankEconomyManager bankEconomyManager;
    private final IFeeProvider feeProvider;
    private final IEventDispatcher eventDispatcher;
    private final ITransactionRounding rounding;
    private final ILogger logger;

    public BankTransactionManager(
            IEconomyManager otherEconomyManager,
            IBankEconomyManager bankEconomyManager,
            IFeeProvider feeProvider,
            IEventDispatcher eventDispatcher,
            ITransactionRounding rounding, ILogger logger
    ) {
        this.otherEconomyManager = otherEconomyManager;
        this.bankEconomyManager = bankEconomyManager;
        this.feeProvider = feeProvider;
        this.eventDispatcher = eventDispatcher;
        this.rounding = rounding;
        this.logger = logger;
    }

    @Override
    public EconomyResult depositToBank(Player player, BigDecimal amount) {
        final var roundedAmount = rounding.round(amount);

        final var currentPlayerBalance = otherEconomyManager.getBalance(player);

        if (currentPlayerBalance.compareTo(roundedAmount) < 0) {
            return new InsufficientFunds(currentPlayerBalance);
        }

        final var fee = feeProvider.getDepositFee(player, roundedAmount);
        final var depositedAmount = roundedAmount.subtract(fee);

        final var depositResult = bankEconomyManager.deposit(player, depositedAmount);

        if (!(depositResult instanceof Success successResult))
            return depositResult;

        // Deposit successful - take from player
        final var takeFromPlayerResult = otherEconomyManager.withdraw(player, roundedAmount);

        if (!(takeFromPlayerResult instanceof Success)) {
            final var rollbackStatus = bankEconomyManager.withdraw(player, depositedAmount);
            if (!(rollbackStatus instanceof Success)) {
                logger.severe(
                        () -> "[ERROR - DEPOSIT] " + player.getName() + " - failed to rollback "
                                + depositedAmount.toPlainString() + " that was deposited to the bank!"
                );
                return new DescribedFailure("Transaction error! Please inform admin!");
            }
            return takeFromPlayerResult;
        }


        eventDispatcher.dispatch(
                new PlayerBankTransactionEvent(player, depositedAmount, fee, TransactionType.DEPOSIT)
        );

        logger.info(
                () -> "[Deposit] " + player.getName() + " deposited "
                        + depositedAmount.toPlainString() + " to the bank + paid fee of " + fee.toPlainString()
        );

        return new Success(successResult.newBalance(), fee);
    }

    @Override
    public EconomyResult withdrawFromBank(Player player, BigDecimal amount) {
        final var roundedAmount = rounding.round(amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return AmountTooSmall.instance;
        }

        final var fee = feeProvider.getWithdrawFee(player, roundedAmount);
        final var takenAmount = roundedAmount.add(fee);
        
        final var withdrawResult = bankEconomyManager.withdraw(player, takenAmount);

        if (withdrawResult instanceof InsufficientFunds isf) {
            final var maximalBalanceFromBank = isf.maximalTransactionAmountAllowed();
            return new InsufficientFunds(feeProvider.getMaximalWithdraw(player, maximalBalanceFromBank));
        }

        if (!(withdrawResult instanceof Success successResult)) {
            return withdrawResult;
        }

        final var giveToPlayerResult = otherEconomyManager.deposit(player, roundedAmount);

        if (!(giveToPlayerResult instanceof Success)) {
            final var rollbackStatus = bankEconomyManager.deposit(player, takenAmount);
            if (!(rollbackStatus instanceof Success)) {
                logger.severe(
                        () -> "[ERROR - WITHDRAW] " + player.getName() + " - failed to rollback "
                                + takenAmount.toPlainString() + " that was withdrawn from the bank!"
                );
                return new DescribedFailure("Transaction error! Please inform admin about this!");
            }
            return giveToPlayerResult;
        }

        eventDispatcher.dispatch(
                new PlayerBankTransactionEvent(player, roundedAmount, fee, TransactionType.WITHDRAW)
        );

        logger.info(
                () -> "[Withdraw] " + player.getName() + " withdrawn " + roundedAmount.toPlainString() + " from the bank + paid fee of " + fee.toPlainString()
        );

        return new Success(
                successResult.newBalance(),
                fee
        );
    }
}
