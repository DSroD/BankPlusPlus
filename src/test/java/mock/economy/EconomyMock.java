package mock.economy;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.AmountTooSmall;
import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.InsufficientFunds;
import dez.fortexx.bankplusplus.api.economy.result.Success;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class EconomyMock implements IEconomyManager {
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
}
