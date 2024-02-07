package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;

@Configuration
public class CommandDescriptionLocalization {
    private String withdraw = "withdraws money from the bank";
    private String deposit = "deposits money to the bank";
    private String balance = "get account balance";
    private String upgrade = "upgrade bank account";
    public String getWithdraw() {
        return withdraw;
    }

    public String getDeposit() {
        return deposit;
    }

    public String getBalance() {
        return balance;
    }

    public String getUpgrade() {
        return upgrade;
    }
}
