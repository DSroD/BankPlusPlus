package dez.fortexx.bankplusplus.utils.formatting;

import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.ItemUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.MoneyUpgradeRequirement;

public class UpgradeRequirementFormatter implements IUpgradeRequirementFormatter {
    private final ICurrencyFormatter currencyFormatter;

    public UpgradeRequirementFormatter(ICurrencyFormatter currencyFormatter) {
        this.currencyFormatter = currencyFormatter;
    }

    @Override
    public String format(IUpgradeRequirement requirement) {
        if (requirement instanceof MoneyUpgradeRequirement m) {
            return currencyFormatter.formatCurrency(m.getAmount());
        }
        if (requirement instanceof ItemUpgradeRequirement i) {
            return i.getItem().getType() + " x" + i.getItem().getAmount();
        }
        // no exhaustive pattern matching in this java version *sad noises*
        throw new UnsupportedOperationException("Unknown upgrade requirement");
    }
}
