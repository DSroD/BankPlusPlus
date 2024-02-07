package dez.fortexx.bankplusplus.bank.levels;

import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;

import java.math.BigDecimal;
import java.util.Set;

public record BankLimit(
        String name,
        BigDecimal maximumMoney,
        Set<IUpgradeRequirement> upgradeRequirements
) {
}
