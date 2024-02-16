package upgrades;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.FailureEconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.SuccessEconomyResult;
import dez.fortexx.bankplusplus.bank.upgrade.MoneyUpgradeRequirement;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyUpgradeRequirementTests {

    @Test
    public void playerHasEnoughMoneySingleManager() {
        final var manager = new MockEconomyManager();
        final var requirement = new MoneyUpgradeRequirement(new BigDecimal("100"), rounding, List.of(manager));
        manager.deposit(null, new BigDecimal("100"));

        final var has = requirement.has(null);
        final var take = requirement.takeFrom(null);

        assertTrue(has);
        assertTrue(take);
        assertEquals(BigDecimal.ZERO, manager.getBalance(null));
    }

    @Test
    public void playerHasEnoughMoneyMultipleMangers1() {
        final var manager1 = new MockEconomyManager();
        final var manager2 = new MockEconomyManager();
        final var requirement = new MoneyUpgradeRequirement(new BigDecimal("10"), rounding, List.of(manager1, manager2));
        manager1.deposit(null, new BigDecimal("100"));
        manager2.deposit(null, new BigDecimal("100"));

        final var has = requirement.has(null);
        final var take = requirement.takeFrom(null);

        assertTrue(has);
        assertTrue(take);
        assertEquals(new BigDecimal("90"), manager1.getBalance(null));
        assertEquals(new BigDecimal("100"), manager2.getBalance(null));
    }

    @Test
    public void playerHasEnoughMoneyMultipleMangers2() {
        final var manager1 = new MockEconomyManager();
        final var manager2 = new MockEconomyManager();
        final var requirement = new MoneyUpgradeRequirement(new BigDecimal("50"), rounding, List.of(manager1, manager2));
        manager1.deposit(null, new BigDecimal("30"));
        manager2.deposit(null, new BigDecimal("30"));

        final var has = requirement.has(null);
        final var take = requirement.takeFrom(null);

        assertTrue(has);
        assertTrue(take);
        assertEquals(BigDecimal.ZERO, manager1.getBalance(null));
        assertEquals(new BigDecimal("10.00"), manager2.getBalance(null));
    }

    @Test
    public void playerHasNotEnoughMoney() {
        final var manager1 = new MockEconomyManager();
        final var manager2 = new MockEconomyManager();

        final var requirement = new MoneyUpgradeRequirement(new BigDecimal("70"), rounding, List.of(manager1, manager2));
        manager1.deposit(null, new BigDecimal("30"));
        manager2.deposit(null, new BigDecimal("30"));

        final var has = requirement.has(null);

        assertFalse(has);
    }

    private final ITransactionRounding rounding = (amount) -> amount.setScale(2, RoundingMode.CEILING);

    private static class MockEconomyManager implements IEconomyManager {
        private BigDecimal amount = BigDecimal.ZERO;
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
    }
}
