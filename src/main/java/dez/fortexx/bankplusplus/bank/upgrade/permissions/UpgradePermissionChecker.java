package dez.fortexx.bankplusplus.bank.upgrade.permissions;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import org.bukkit.entity.Player;

public class UpgradePermissionChecker implements IUpgradePermissionChecker {
    @Override
    public boolean canUpgrade(Player p, BankLimit limit) {
        final var permission = getLimitPermissionParentNode() + "." + getLimitPermissionSubNode(limit);
        return p.hasPermission(permission);
    }

    @Override
    public String getLimitPermissionSubNode(BankLimit limit) {
        return limit.name()
                .toLowerCase()
                .replace(' ', '_');
    }

    @Override
    public String getLimitPermissionParentNode() {
        return "bank.upgrade";
    }
}
