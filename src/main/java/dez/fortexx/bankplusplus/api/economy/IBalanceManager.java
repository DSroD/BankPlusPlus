package dez.fortexx.bankplusplus.api.economy;

import dez.fortexx.bankplusplus.api.economy.transaction.ITransactionResult;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public interface IBalanceManager {
    BigDecimal getBalance(OfflinePlayer player);
    ITransactionResult deposit(OfflinePlayer player, BigDecimal amount);
    ITransactionResult withdraw(OfflinePlayer player, BigDecimal amount);
}
