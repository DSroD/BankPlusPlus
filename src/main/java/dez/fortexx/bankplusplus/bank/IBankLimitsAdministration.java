package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public interface IBankLimitsAdministration {
    /**
     * Upgrades the bank limits of given player, not using any of the requirements and not checking player permissions
     * @param offlinePlayer Player
     * @return New bank limit or empty if already at max level
     */
    Optional<BankLimit> forceUpgradeLimits(OfflinePlayer offlinePlayer);

    /**
     * Downgrades the bank limits of given player
     * @param offlinePlayer Player
     * @return New bank limit or empty if already at min level
     */
    Optional<BankLimit> forceDowngradeLimit(OfflinePlayer offlinePlayer);
}
