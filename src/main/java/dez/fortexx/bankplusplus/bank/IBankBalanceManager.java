package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.bank.transaction.ITransactionResult;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface IBankBalanceManager {
    BigDecimal getBalance(Player player);
    ITransactionResult deposit(Player player, BigDecimal amount);
    ITransactionResult withdraw(Player player, BigDecimal amount);
}
