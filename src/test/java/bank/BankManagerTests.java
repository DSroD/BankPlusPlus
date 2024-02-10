package bank;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.FailureEconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.SuccessEconomyResult;
import dez.fortexx.bankplusplus.bank.BankManager;
import dez.fortexx.bankplusplus.bank.balance.IBankEconomyManager;
import dez.fortexx.bankplusplus.bank.fees.IFeeProvider;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.transaction.AmountTooSmallTransactionResult;
import dez.fortexx.bankplusplus.bank.transaction.InsufficientFundsTransactionResult;
import dez.fortexx.bankplusplus.bank.transaction.LimitsViolationsTransactionResult;
import dez.fortexx.bankplusplus.bank.transaction.SuccessTransactionResult;
import dez.fortexx.bankplusplus.bank.upgrade.MoneyUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.IUpgradePermissionChecker;
import dez.fortexx.bankplusplus.bank.upgrade.result.MaxLevelUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingPermissionsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingRequirementsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.SuccessUpgradeResult;
import dez.fortexx.bankplusplus.configuration.BankLevelConfig;
import dez.fortexx.bankplusplus.configuration.UpgradeRequirementConfig;
import dez.fortexx.bankplusplus.events.IEventDispatcher;
import dez.fortexx.bankplusplus.events.PlayerBankTransactionEvent;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankManagerTests {

    private IBankEconomyManager bankEconomy;
    private IBankEconomyManager otherEconomy;
    private BankManager bankManager;
    private EventDispatcherMock eventDispatcherMock;

    @BeforeEach
    public void setup() {
        bankEconomy = new BankEconomyMock();
        otherEconomy = new BankEconomyMock();
        eventDispatcherMock = new EventDispatcherMock();
        bankManager = new BankManager(
                createLimits(List.of(otherEconomy, bankEconomy)),
                otherEconomy,
                bankEconomy,
                new UpgradePermissionCheckerMock(true),
                new FeeProviderMock(BigDecimal.ONE),
                eventDispatcherMock,
                rounding
        );
    }

    @Test
    public void withdrawTest() {
        final var result1 = bankManager.withdraw(null, BigDecimal.ONE);
        assertTrue(result1 instanceof InsufficientFundsTransactionResult);

        bankEconomy.deposit(null, BigDecimal.ONE); // directly add 1 to the bank

        final var result2 = bankManager.withdraw(null, BigDecimal.ONE);
        assertTrue(result2 instanceof InsufficientFundsTransactionResult);

        bankEconomy.deposit(null, BigDecimal.ONE); // Add 1 more - 2 should be in the bank

        // Withdraw 1 + 1 fee
        final var result3 = bankManager.withdraw(null, BigDecimal.ONE);
        assertEquals(new SuccessTransactionResult(new BigDecimal("0.00"), BigDecimal.ONE), result3);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        assertTrue(eventDispatcherMock.wasCalledWith(PlayerBankTransactionEvent.class));
    }

    @Test
    public void depositTest() {
        final var result1 = bankManager.deposit(null, BigDecimal.ONE);
        assertTrue(result1 instanceof InsufficientFundsTransactionResult);

        otherEconomy.deposit(null, BigDecimal.ONE);

        final var result2 = bankManager.deposit(null, BigDecimal.ONE);
        assertEquals(AmountTooSmallTransactionResult.instance, result2);

        final var result3 = bankManager.deposit(null, new BigDecimal("-1"));
        assertEquals(AmountTooSmallTransactionResult.instance, result3);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        otherEconomy.deposit(null, BigDecimal.TEN);
        final var result4 = bankManager.deposit(null, BigDecimal.TEN);
        assertEquals(new SuccessTransactionResult(new BigDecimal("9.00"), BigDecimal.ONE), result4);
        assertEquals(0, BigDecimal.ONE.compareTo(otherEconomy.getBalance(null)));

        assertTrue(eventDispatcherMock.wasCalledWith(PlayerBankTransactionEvent.class));
    }

    @Test
    public void roundingTestDeposit() {
        otherEconomy.deposit(null, BigDecimal.TEN);

        final var depositResult = bankManager.deposit(null, new BigDecimal("1.001"));
        assertEquals(new SuccessTransactionResult(new BigDecimal("0.01"), BigDecimal.ONE), depositResult);
        assertEquals(0, new BigDecimal("8.99").compareTo(otherEconomy.getBalance(null)));
    }

    @Test
    public void roundingTestWithdraw() {
        bankEconomy.deposit(null, BigDecimal.TEN);

        final var depositResult = bankManager.withdraw(null, new BigDecimal("1.001"));
        assertEquals(new SuccessTransactionResult(new BigDecimal("7.99"), BigDecimal.ONE), depositResult);
        assertEquals(0, new BigDecimal("1.01").compareTo(otherEconomy.getBalance(null)));
    }

    @Test
    public void testUpgradeInsufficientBalance() {
        bankEconomy.deposit(null, BigDecimal.ONE);
        otherEconomy.deposit(null, BigDecimal.ONE);
        final var result1 = bankManager.upgradeLimits(null);
        assertTrue(result1 instanceof MissingRequirementsUpgradeResult);
        assertEquals(1, ((MissingRequirementsUpgradeResult) result1).missingRequirements().size());
        assertTrue(((MissingRequirementsUpgradeResult) result1).missingRequirements().get(0) instanceof MoneyUpgradeRequirement);
        assertEquals(
                BigDecimal.TEN,
                ((MoneyUpgradeRequirement)
                        ((MissingRequirementsUpgradeResult) result1)
                                .missingRequirements().get(0))
                        .getAmount()
        );
        assertEquals(1, bankEconomy.getBankLevel(null));
        assertEquals(BigDecimal.ONE, bankEconomy.getBalance(null));
        assertEquals(BigDecimal.ONE, otherEconomy.getBalance(null));
    }

    @Test
    public void testUpgradeUsingBankEconomy() {
        bankEconomy.deposit(null, BigDecimal.TEN);
        final var result = bankManager.upgradeLimits(null);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(BigDecimal.ZERO, bankEconomy.getBalance(null));
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(null));
        assertEquals(2, bankEconomy.getBankLevel(null));
    }

    @Test
    public void testUpgradeUsingOtherEconomy() {
        bankEconomy.deposit(null, BigDecimal.TEN);
        otherEconomy.deposit(null, BigDecimal.TEN);
        final var result = bankManager.upgradeLimits(null);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(BigDecimal.TEN, bankEconomy.getBalance(null));
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(null));
        assertEquals(2, bankEconomy.getBankLevel(null));
    }

    @Test
    public void testUpgradeUsingBothEconomies() {
        bankEconomy.deposit(null, new BigDecimal("6"));
        otherEconomy.deposit(null, new BigDecimal("6"));
        final var result = bankManager.upgradeLimits(null);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(null));
        assertEquals(new BigDecimal("2"), bankEconomy.getBalance(null));
        assertEquals(2, bankEconomy.getBankLevel(null));
    }

    @Test
    public void testMaxLevelUpgrade() {
        bankEconomy.deposit(null, BigDecimal.TEN);

        final var upgrade1 = bankManager.upgradeLimits(null);
        assertTrue(upgrade1 instanceof SuccessUpgradeResult);
        final var upgrade2 = bankManager.upgradeLimits(null);
        assertTrue(upgrade2 instanceof MaxLevelUpgradeResult);
    }

    @Test
    public void testLimits() {
        bankEconomy.deposit(null, BigDecimal.ZERO);
        otherEconomy.deposit(null, new BigDecimal("300"));

        final var l1limit = bankManager.getLimit(null);
        assertEquals(new BigDecimal("100"), l1limit.maximumMoney());

        final var depositOverLimitResult = bankManager.deposit(null, new BigDecimal("200"));
        assertEquals(LimitsViolationsTransactionResult.instance, depositOverLimitResult);

        // otherEconomy -10, currently has 290
        final var upgradeResult = bankManager.upgradeLimits(null);
        assertTrue(upgradeResult instanceof SuccessUpgradeResult);

        // otherEconomy -200, currenlty has 90
        final var depositUnderLimitResult = bankManager.deposit(null, new BigDecimal("200"));
        assertTrue(depositUnderLimitResult instanceof SuccessTransactionResult);

        // deposit remaining 90 - this is over 200 limit of level 2
        final var depositOverL2Limit = bankManager.deposit(null, new BigDecimal("90"));
        assertEquals(LimitsViolationsTransactionResult.instance, depositOverL2Limit);
    }

    @Test
    public void testNextLevelRequirements() {
        bankEconomy.deposit(null, BigDecimal.TEN);

        final var nextLevel = bankManager.getNextLevelLimit(null);
        assertTrue(nextLevel.isPresent());
        assertEquals("L2", nextLevel.get().name());

        final var upgrade1 = bankManager.upgradeLimits(null);
        assertTrue(upgrade1 instanceof SuccessUpgradeResult);

        final var emptyLevel = bankManager.getNextLevelLimit(null);
        assertTrue(emptyLevel.isEmpty());
    }

    @Test
    public void getBalanceTest() {
        assertEquals(BigDecimal.ZERO, bankManager.getBalance(null));
        bankEconomy.deposit(null, BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, bankManager.getBalance(null));
    }

    private List<BankLimit> createLimits(List<IEconomyManager> managers) {
        return Stream.of(
                BankLevelConfig.of(new BigDecimal("100"), "L1"),
                BankLevelConfig.of(
                        new BigDecimal("200"), "L2",
                        l2LimitsRequirements)
        ).map(c -> c.toBankLimit(managers)).toList();
    }

    @Test
    public void upgradeWithoutPermissionsTest() {
        final var bankManager = new BankManager(
                createLimits(List.of(otherEconomy, bankEconomy)),
                otherEconomy,
                bankEconomy,
                new UpgradePermissionCheckerMock(false),
                new FeeProviderMock(BigDecimal.ONE),
                eventDispatcherMock,
                rounding
        );

        bankEconomy.deposit(null, BigDecimal.TEN);
        final var upgradeResult = bankManager.upgradeLimits(null);

        assertEquals(MissingPermissionsUpgradeResult.instance, upgradeResult);
    }

    private final UpgradeRequirementConfig l2LimitsRequirements =
            UpgradeRequirementConfig.of(BigDecimal.TEN, List.of());

    private final ITransactionRounding rounding = (amount) -> amount.setScale(2, RoundingMode.CEILING);
    private static class EventDispatcherMock implements IEventDispatcher {

        private final Set<Class<? extends Event>> calledWith = new HashSet<>();
        @Override
        public void dispatch(Event e) {
            calledWith.add(e.getClass());
        }

        public boolean wasCalledWith(Class<? extends Event> cls) {
            return calledWith.contains(cls);
        }
    }

    private record FeeProviderMock(BigDecimal amount) implements IFeeProvider {

        @Override
        public BigDecimal getDepositFee(OfflinePlayer player, BigDecimal amount) {
            return this.amount;
        }

        @Override
        public BigDecimal getWithdrawFee(OfflinePlayer player, BigDecimal amount) {
                return this.amount;
            }

        @Override
        public BigDecimal getMaximalWithdraw(OfflinePlayer player, BigDecimal accountBalance) {
            return accountBalance.subtract(this.amount);
        }
    }

    private record UpgradePermissionCheckerMock(boolean canUpgrade) implements IUpgradePermissionChecker {

        @Override
            public boolean canUpgrade(Player p, BankLimit limit) {
                return canUpgrade;
            }

            @Override
            public String getLimitPermissionSubNode(BankLimit limit) {
                return null;
            }

            @Override
            public String getLimitPermissionParentNode() {
                return null;
            }
        }

    private static class BankEconomyMock implements IBankEconomyManager {
        private BigDecimal amount = BigDecimal.ZERO;
        private int level = 1;

        @Override
        public EconomyResult deposit(OfflinePlayer player, BigDecimal amount) {
            this.amount = this.amount.add(amount);
            return SuccessEconomyResult.instance;
        }

        @Override
        public EconomyResult withdraw(OfflinePlayer player, BigDecimal amount) {
            if (this.amount.compareTo(amount) < 0) {
                return FailureEconomyResult.instance;
            }
            this.amount = this.amount.subtract(amount);
            return SuccessEconomyResult.instance;
        }

        @Override
        public BigDecimal getBalance(OfflinePlayer player) {
            return amount;
        }

        @Override
        public boolean upgradeLevel(OfflinePlayer player) {
            level++;
            return true;
        }

        @Override
        public int getBankLevel(OfflinePlayer player) {
            return level;
        }
    }
}
