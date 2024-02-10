package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.result.IUpgradeResult;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface IBankLimitsManager {
    IUpgradeResult upgradeLimits(Player player);

    BankLimit getLimit(Player player);
    Optional<BankLimit> getNextLevelLimit(Player player);
}
