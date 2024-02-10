package dez.fortexx.bankplusplus.api.economy;

import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public interface IEconomyManager {
    EconomyResult deposit(OfflinePlayer player, BigDecimal amount);
    EconomyResult withdraw(OfflinePlayer player, BigDecimal amount);
    BigDecimal getBalance(OfflinePlayer player);
}
