package dez.fortexx.bankplusplus.bank.fees;

import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageFeeProvider implements IFeeProvider {

    private final BigDecimal depositFeePercentage;
    private final BigDecimal withdrawFeePercentage;
    private final ITransactionRounding rounding;

    public PercentageFeeProvider(
            BigDecimal depositFeePercentage,
            BigDecimal withdrawFeePercentage,
            ITransactionRounding rounding
    ) {
        this.depositFeePercentage = depositFeePercentage;
        this.withdrawFeePercentage = withdrawFeePercentage;
        this.rounding = rounding;
    }

    @Override
    public BigDecimal getDepositFee(OfflinePlayer player, BigDecimal amount) {
        final var p = player.getPlayer();
        if (p != null && p.hasPermission("bankplusplus.fees.bypass.deposit")) {
            return BigDecimal.ZERO;
        }
        return rounding.round(amount.multiply(depositFeePercentage));
    }

    @Override
    public BigDecimal getWithdrawFee(OfflinePlayer player, BigDecimal amount) {
        final var p = player.getPlayer();
        if (p != null && p.hasPermission("bankplusplus.fees.bypass.withdraw")) {
            return BigDecimal.ZERO;
        }
        return rounding.round(amount.multiply(withdrawFeePercentage));
    }

    @Override
    public BigDecimal getMaximalWithdraw(OfflinePlayer player, BigDecimal accountBalance) {
        final var p = player.getPlayer();
        if (p != null && p.hasPermission("bankplusplus.fees.bypass.withdraw")) {
            return accountBalance;
        }
        // Withdraw is total_taken = asked_for + asked_for * fee_percentage
        // so asked_for = total_taken / (1 + fee_percentage)
        final var divisor = BigDecimal.ONE.add(withdrawFeePercentage);
        return accountBalance.divide(divisor, RoundingMode.FLOOR);
    }
}
