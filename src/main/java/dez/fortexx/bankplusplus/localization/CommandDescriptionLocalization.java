package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class CommandDescriptionLocalization {
    private String withdraw = "withdraws money from the bank";
    private String deposit = "deposits money to the bank";
    private String balance = "get account balance";
    private String upgrade = "upgrade bank account";
    private String info = "bank account info";
    public String getWithdrawCommandDescription() {
        return withdraw;
    }

    public String getDepositCommandDescription() {
        return deposit;
    }

    public String getBalanceCommandDescription() {
        return balance;
    }

    public String getUpgradeCommandDescription() {
        return upgrade;
    }

    public String getInfoCommandDescription() {
        return info;
    }
}
