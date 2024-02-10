package dez.fortexx.bankplusplus.bank.upgrade.permissions;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import org.bukkit.entity.Player;

public interface IUpgradePermissionChecker {
    boolean canUpgrade(Player p, BankLimit limit);

    String getLimitPermissionSubNode(BankLimit limit);

    String getLimitPermissionParentNode();
}
