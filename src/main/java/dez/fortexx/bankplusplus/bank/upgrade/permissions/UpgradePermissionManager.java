package dez.fortexx.bankplusplus.bank.upgrade.permissions;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import org.bukkit.entity.Player;

import java.text.Normalizer;

public class UpgradePermissionManager implements IUpgradePermissionManager {
    private final String parentNode;

    public UpgradePermissionManager(String parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public boolean canUpgrade(Player p, BankLimit limit) {
        final var permission = getLimitPermissionNode(limit);
        return p.hasPermission(permission);
    }

    @Override
    public String getLimitPermissionNode(BankLimit limit) {
        final var subnode = limit.name()
                .toLowerCase()
                .replace(' ', '_');
        final var node = parentNode + "." + subnode;
        // https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
        return Normalizer.normalize(node, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    @Override
    public String getLimitPermissionParentNode() {
        return parentNode;
    }
}
