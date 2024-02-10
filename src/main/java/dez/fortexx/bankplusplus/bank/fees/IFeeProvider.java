package dez.fortexx.bankplusplus.bank.fees;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public interface IFeeProvider {
    BigDecimal getDepositFee(OfflinePlayer player, BigDecimal amount);
    BigDecimal getWithdrawFee(OfflinePlayer player, BigDecimal amount);

    BigDecimal getMaximalWithdraw(OfflinePlayer player, BigDecimal accountBalance);
}
