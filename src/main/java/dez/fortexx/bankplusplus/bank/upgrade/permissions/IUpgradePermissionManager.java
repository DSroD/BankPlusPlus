package dez.fortexx.bankplusplus.bank.upgrade.permissions;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import org.bukkit.entity.Player;

public interface IUpgradePermissionManager {
    boolean canUpgrade(Player p, BankLimit limit);

    String getLimitPermissionNode(BankLimit limit);

    String getLimitPermissionParentNode();
}
