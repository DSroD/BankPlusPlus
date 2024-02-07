package dez.fortexx.bankplusplus.bank;

import dez.fortexx.bankplusplus.bank.levels.BankLimit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public interface IBankLimitsManager {
    // TODO: return type distinguish failure causes
    boolean upgradeLimits(Player player);

    BankLimit getLimit(Player player);
}
