package mock.fees;

import dez.fortexx.bankplusplus.bank.fees.IFeeProvider;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public record FeeProviderMock(BigDecimal amount) implements IFeeProvider {

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
