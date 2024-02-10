package dez.fortexx.bankplusplus.bank.upgrade.result;

import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;

import java.util.List;

public record MissingRequirementsUpgradeResult(
    List<IUpgradeRequirement> missingRequirements
) implements IUpgradeResult {
}
