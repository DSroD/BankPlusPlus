package dez.fortexx.bankplusplus.bank.fees;

import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageFeeProvider implements IFeeProvider {

    private final BigDecimal depositTaxPercentage;
    private final BigDecimal withdrawTaxPercentage;

    private final int decimalScale;

    public PercentageFeeProvider(BigDecimal depositTaxPercentage, BigDecimal withdrawTaxPercentage, int decimalScale) {
        this.depositTaxPercentage = depositTaxPercentage;
        this.withdrawTaxPercentage = withdrawTaxPercentage;
        this.decimalScale = decimalScale;
    }

    @Override
    public BigDecimal getDepositFee(OfflinePlayer player, BigDecimal amount) {
        final var p = player.getPlayer();
        if (p != null && p.hasPermission("bank.fees.bypass.deposit")) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(depositTaxPercentage)
                .setScale(decimalScale, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal getWithdrawFee(OfflinePlayer player, BigDecimal amount) {
        final var p = player.getPlayer();
        if (p != null && p.hasPermission("bank.fees.bypass.withdraw")) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(withdrawTaxPercentage)
                .setScale(decimalScale, RoundingMode.CEILING);
    }
}
