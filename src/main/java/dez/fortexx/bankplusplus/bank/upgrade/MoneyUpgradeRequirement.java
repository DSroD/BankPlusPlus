package dez.fortexx.bankplusplus.bank.upgrade;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;

public final class MoneyUpgradeRequirement implements IUpgradeRequirement {
    private final BigDecimal amount;
    private final ITransactionRounding rounding;
    private final List<IEconomyManager> balanceManagers;

    public MoneyUpgradeRequirement(
            BigDecimal amount,
            ITransactionRounding rounding,
            List<IEconomyManager> balanceManagers
    ) {
        this.amount = amount;
        this.rounding = rounding;
        this.balanceManagers = balanceManagers;
    }

    @Override
    public boolean has(Player p) {
        var balanceAvailable = BigDecimal.ZERO;
        for (final var balanceManager : balanceManagers) {
            balanceAvailable = balanceAvailable.add(balanceManager.getBalance(p));
            if (balanceAvailable.compareTo(amount) >= 0)
                return true;
        }
        return false;
    }

    // TODO: use transaction to avoid whoopsies
    @Override
    public boolean takeFrom(Player p) {
        var balanceTaken = BigDecimal.ZERO;
        for (final var balanceManager : balanceManagers) {
            // remaining = amount - balanceTaken
            var balanceAvailable = balanceManager.getBalance(p);
            if (balanceTaken.add(balanceAvailable).compareTo(amount) >= 0) {

                var toTake = amount.subtract(balanceTaken);
                balanceManager.withdraw(p, toTake);
                return true;
            }

            balanceManager.withdraw(p, balanceAvailable);
            // This might cause a tiny error in calculation, but it will
            // keep the amounts taken nice
            balanceTaken = rounding.round(balanceTaken.add(balanceAvailable));
        }
        return false;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
