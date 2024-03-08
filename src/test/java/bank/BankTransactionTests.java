package bank;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.AmountTooSmall;
import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.InsufficientFunds;
import dez.fortexx.bankplusplus.api.economy.result.Success;
import dez.fortexx.bankplusplus.bank.BankTransactionManager;
import dez.fortexx.bankplusplus.bank.balance.IBankEconomyManager;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.result.IUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.SuccessUpgradeResult;
import dez.fortexx.bankplusplus.events.PlayerBankTransactionEvent;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import mock.LoggerMock;
import mock.economy.EconomyMock;
import mock.events.EventDispatcherMock;
import mock.fees.FeeProviderMock;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankTransactionTests {

    private IBankEconomyManager bankEconomy;
    private IEconomyManager otherEconomy;
    private BankTransactionManager bankManager;
    private EventDispatcherMock eventDispatcherMock;

    @BeforeEach
    public void setup() {
        bankEconomy = new BankEconomyMock();
        otherEconomy = new EconomyMock();
        eventDispatcherMock = new EventDispatcherMock();
        bankManager = new BankTransactionManager(
                otherEconomy,
                bankEconomy,
                new FeeProviderMock(BigDecimal.ONE),
                eventDispatcherMock,
                rounding,
                new LoggerMock()
        );
    }

    @Test
    public void withdrawTest() {
        final var result1 = bankManager.withdrawFromBank(null, BigDecimal.ONE);
        assertTrue(result1 instanceof InsufficientFunds);

        bankEconomy.deposit(null, BigDecimal.ONE); // directly add 1 to the bank

        final var result2 = bankManager.withdrawFromBank(null, BigDecimal.ONE);
        assertTrue(result2 instanceof InsufficientFunds);

        bankEconomy.deposit(null, BigDecimal.ONE); // Add 1 more - 2 should be in the bank

        // Withdraw 1 + 1 fee
        final var result3 = bankManager.withdrawFromBank(null, BigDecimal.ONE);
        assertEquals(new Success(new BigDecimal("0.00"), BigDecimal.ONE), result3);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        final var result4 = bankManager.withdrawFromBank(null, BigDecimal.ZERO);
        assertTrue(result4 instanceof AmountTooSmall);

        assertTrue(eventDispatcherMock.wasCalledWith(PlayerBankTransactionEvent.class));
    }

    @Test
    public void depositTest() {
        final var result1 = bankManager.depositToBank(null, BigDecimal.ONE);
        assertTrue(result1 instanceof InsufficientFunds);

        otherEconomy.deposit(null, BigDecimal.ONE);

        final var result2 = bankManager.depositToBank(null, BigDecimal.ONE);
        assertEquals(AmountTooSmall.instance, result2);

        final var result3 = bankManager.depositToBank(null, new BigDecimal("-1"));
        assertEquals(AmountTooSmall.instance, result3);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        otherEconomy.deposit(null, BigDecimal.TEN);
        final var result4 = bankManager.depositToBank(null, BigDecimal.TEN);
        assertEquals(new Success(new BigDecimal("9.00"), BigDecimal.ONE), result4);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        assertTrue(eventDispatcherMock.wasCalledWith(PlayerBankTransactionEvent.class));
    }

    @Test
    public void roundingTestDeposit() {
        otherEconomy.deposit(null, BigDecimal.TEN);

        final var depositResult = bankManager.depositToBank(null, new BigDecimal("1.001"));
        assertEquals(new Success(new BigDecimal("0.01"), BigDecimal.ONE), depositResult);
        assertEquals(0, new BigDecimal("8.99").compareTo(otherEconomy.getBalance(null)));
    }

    @Test
    public void roundingTestWithdraw() {
        bankEconomy.deposit(null, BigDecimal.TEN);

        final var depositResult = bankManager.withdrawFromBank(null, new BigDecimal("1.001"));
        assertEquals(new Success(new BigDecimal("7.99"), BigDecimal.ONE), depositResult);
        assertEquals(0, new BigDecimal("1.01").compareTo(otherEconomy.getBalance(null)));
    }

    private final ITransactionRounding rounding = (amount) -> amount.setScale(2, RoundingMode.CEILING);

    private static class BankEconomyMock implements IBankEconomyManager {
        private BigDecimal amount = BigDecimal.ZERO;

        @Override
        public EconomyResult deposit(OfflinePlayer player, BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return AmountTooSmall.instance;
            }
            this.amount = this.amount.add(amount);
            return new Success(this.amount, BigDecimal.ZERO);
        }

        @Override
        public EconomyResult withdraw(OfflinePlayer player, BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return AmountTooSmall.instance;
            }
            if (this.amount.compareTo(amount) < 0) {
                return new InsufficientFunds(amount);
            }
            this.amount = this.amount.subtract(amount);
            return new Success(this.amount, BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBalance(OfflinePlayer player) {
            return amount;
        }

        // Unused in tests
        @Override
        public IUpgradeResult upgradeLimits(Player player) {
            return SuccessUpgradeResult.instance;
        }

        // Unused in tests
        @Override
        public BankLimit getBankLevelLimit(OfflinePlayer player) {
            return null;
        }

        // Unused in tests
        @Override
        public Optional<BankLimit> getNextBankLevelLimit(OfflinePlayer player) {
            return Optional.empty();
        }
    }
}
