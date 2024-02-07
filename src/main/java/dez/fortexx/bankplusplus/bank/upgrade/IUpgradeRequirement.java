package dez.fortexx.bankplusplus.bank.upgrade;

import org.bukkit.entity.Player;

public sealed interface IUpgradeRequirement permits ItemUpgradeRequirement, MoneyUpgradeRequirement {
    boolean has(Player p);
    boolean takeFrom(Player p);
}
