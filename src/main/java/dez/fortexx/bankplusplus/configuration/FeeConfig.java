package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

import java.math.BigDecimal;
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class FeeConfig {

    @Comment("Deposit fee (0-1)")
    private BigDecimal depositFeePercent = new BigDecimal("0.05");
    @Comment("Withdraw fee (0-1)")
    private BigDecimal withdrawFeePercent = new BigDecimal("0.05");

    public BigDecimal getDepositFeePercent() {
        return depositFeePercent;
    }

    public BigDecimal getWithdrawFeePercent() {
        return withdrawFeePercent;
    }
}
