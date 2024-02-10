package dez.fortexx.bankplusplus.bank.upgrade.result;

public sealed interface IUpgradeResult permits MaxLevelUpgradeResult, MissingPermissionsUpgradeResult, MissingRequirementsUpgradeResult, SuccessUpgradeResult {
}
