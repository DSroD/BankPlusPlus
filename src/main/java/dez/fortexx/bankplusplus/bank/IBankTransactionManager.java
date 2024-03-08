package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface IBankTransactionManager {
    /**
     * Deposits to the bank from other economy. Returns the result of
     * bank deposit action
     * @param player Player
     * @param amount Amount
     * @return Result of the deposition
     */
    EconomyResult depositToBank(Player player, BigDecimal amount);

    /**
     * Withdraws from the bank to the other economy. Returns the result
     * of bank withdrawal
     * @param player Player
     * @param amount Amount
     * @return Result of the withdrawal
     */
    EconomyResult withdrawFromBank(Player player, BigDecimal amount);
}
