package dez.fortexx.bankplusplus.bank.balance;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import org.bukkit.OfflinePlayer;

public interface IBankEconomyManager extends IEconomyManager {
    boolean upgradeLevel(OfflinePlayer player);
    int getBankLevel(OfflinePlayer player);
}
