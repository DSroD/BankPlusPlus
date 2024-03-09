package bank;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.LimitViolation;
import dez.fortexx.bankplusplus.api.economy.result.Success;
import dez.fortexx.bankplusplus.bank.BankEconomyManager;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.MoneyUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.IUpgradePermissionManager;
import dez.fortexx.bankplusplus.bank.upgrade.result.MaxLevelUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingPermissionsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.MissingRequirementsUpgradeResult;
import dez.fortexx.bankplusplus.bank.upgrade.result.SuccessUpgradeResult;
import dez.fortexx.bankplusplus.configuration.BankLevelConfig;
import dez.fortexx.bankplusplus.configuration.UpgradeRequirementConfig;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import mock.LoggerMock;
import mock.economy.EconomyMock;
import mock.events.EventDispatcherMock;
import mock.player.PlayerMock;
import mock.store.BankStoreMock;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankEconomyManagerTests {
    private BankEconomyManager bankEconomy;
    private IEconomyManager otherEconomy;
    private IBankStore bankStoreMock;
    private List<BankLimit> limits;
    private final Player playerMock = new PlayerMock();

    @BeforeEach
    public void setup() {
        bankStoreMock = new BankStoreMock();
        otherEconomy = new EconomyMock();
        bankEconomy = new BankEconomyManager(
                bankStoreMock,
                new UpgradePermissionManagerMock(true),
                new EventDispatcherMock(),
                new LoggerMock()
        );

        limits = createLimits(List.of(otherEconomy, bankEconomy));
        bankEconomy.setBankLimits(limits);
    }

    @Test
    public void testUpgradeInsufficientBalance() {
        bankEconomy.deposit(playerMock, BigDecimal.ONE);
        otherEconomy.deposit(playerMock, BigDecimal.ONE);
        final var result1 = bankEconomy.upgradeLimits(playerMock);
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
        assertEquals(limits.get(0), bankEconomy.getBankLevelLimit(playerMock));
        assertEquals(BigDecimal.ONE, bankEconomy.getBalance(playerMock));
        assertEquals(BigDecimal.ONE, otherEconomy.getBalance(playerMock));
    }

    @Test
    public void testUpgradeUsingBankEconomy() {
        bankEconomy.deposit(playerMock, BigDecimal.TEN);
        final var result = bankEconomy.upgradeLimits(playerMock);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(rounding.round(BigDecimal.ZERO), bankEconomy.getBalance(playerMock));
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(playerMock));
        assertEquals(limits.get(1), bankEconomy.getBankLevelLimit(playerMock));
    }

    @Test
    public void testUpgradeUsingOtherEconomy() {
        bankEconomy.deposit(playerMock, BigDecimal.TEN);
        otherEconomy.deposit(playerMock, BigDecimal.TEN);
        final var result = bankEconomy.upgradeLimits(playerMock);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(BigDecimal.TEN, bankEconomy.getBalance(playerMock));
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(playerMock));
        assertEquals(limits.get(1), bankEconomy.getBankLevelLimit(playerMock));
    }

    @Test
    public void testUpgradeUsingBothEconomies() {
        bankEconomy.deposit(playerMock, new BigDecimal("6"));
        otherEconomy.deposit(playerMock, new BigDecimal("6"));
        final var result = bankEconomy.upgradeLimits(playerMock);
        assertTrue(result instanceof SuccessUpgradeResult);
        assertEquals(BigDecimal.ZERO, otherEconomy.getBalance(playerMock));
        assertEquals(new BigDecimal("2.00"), bankEconomy.getBalance(playerMock));
        assertEquals(limits.get(1), bankEconomy.getBankLevelLimit(playerMock));
    }

    @Test
    public void testMaxLevelUpgrade() {
        bankEconomy.deposit(playerMock, BigDecimal.TEN);

        final var upgrade1 = bankEconomy.upgradeLimits(playerMock);
        assertTrue(upgrade1 instanceof SuccessUpgradeResult);
        final var upgrade2 = bankEconomy.upgradeLimits(playerMock);
        assertTrue(upgrade2 instanceof MaxLevelUpgradeResult);
    }

    @Test
    public void testLimits() {
        bankEconomy.deposit(playerMock, BigDecimal.ZERO);
        otherEconomy.deposit(playerMock, new BigDecimal("300"));

        final var l1limit = bankEconomy.getBankLevelLimit(playerMock);
        assertEquals(new BigDecimal("100"), l1limit.maximumMoney());

        final var depositOverLimitResult = bankEconomy.deposit(playerMock, new BigDecimal("200"));
        assertEquals(LimitViolation.instance, depositOverLimitResult);

        // otherEconomy -10, currently has 290
        final var upgradeResult = bankEconomy.upgradeLimits(playerMock);
        assertTrue(upgradeResult instanceof SuccessUpgradeResult);

        // otherEconomy -200, currenlty has 90
        final var depositUnderLimitResult = bankEconomy.deposit(playerMock, new BigDecimal("200"));
        assertTrue(depositUnderLimitResult instanceof Success);

        // deposit remaining 90 - this is over 200 limit of level 2
        final var depositOverL2Limit = bankEconomy.deposit(playerMock, new BigDecimal("90"));
        assertEquals(LimitViolation.instance, depositOverL2Limit);
    }

    @Test
    public void testNextLevelRequirements() {
        bankEconomy.deposit(playerMock, BigDecimal.TEN);

        final var nextLevel = bankEconomy.getNextBankLevelLimit(playerMock);
        assertTrue(nextLevel.isPresent());
        assertEquals("L2", nextLevel.get().name());

        final var upgrade1 = bankEconomy.upgradeLimits(playerMock);
        assertTrue(upgrade1 instanceof SuccessUpgradeResult);

        final var emptyLevel = bankEconomy.getNextBankLevelLimit(playerMock);
        assertTrue(emptyLevel.isEmpty());
    }

    @Test
    public void getBalanceTest() {
        assertEquals(BigDecimal.ZERO, bankEconomy.getBalance(playerMock));
        bankEconomy.deposit(playerMock, BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, bankEconomy.getBalance(playerMock));
    }

    @Test
    public void upgradeWithoutPermissionsTest() {
        final var bankEconomy = new BankEconomyManager(
                bankStoreMock,
                new UpgradePermissionManagerMock(false),
                new EventDispatcherMock(),
                new LoggerMock()
        );

        bankEconomy.setBankLimits(createLimits(List.of(otherEconomy, bankEconomy)));

        bankEconomy.deposit(playerMock, BigDecimal.TEN);
        final var upgradeResult = bankEconomy.upgradeLimits(playerMock);

        assertEquals(MissingPermissionsUpgradeResult.instance, upgradeResult);
    }

    private List<BankLimit> createLimits(List<IEconomyManager> managers) {
        return Stream.of(
                BankLevelConfig.of(new BigDecimal("100"), "L1"),
                BankLevelConfig.of(
                        new BigDecimal("200"), "L2",
                        l2LimitsRequirements)
        ).map(c -> c.toBankLimit(managers, rounding)).toList();
    }

    private final UpgradeRequirementConfig l2LimitsRequirements =
            UpgradeRequirementConfig.of(BigDecimal.TEN, List.of());

    private final ITransactionRounding rounding = (amount) -> amount.setScale(2, RoundingMode.CEILING);

    private record UpgradePermissionManagerMock(boolean canUpgrade) implements IUpgradePermissionManager {

        @Override
        public boolean canUpgrade(Player p, BankLimit limit) {
            return canUpgrade;
        }

        @Override
        public String getLimitPermissionNode(BankLimit limit) {
            return null;
        }
    }

}
