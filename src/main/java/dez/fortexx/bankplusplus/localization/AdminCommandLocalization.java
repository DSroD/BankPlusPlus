package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class AdminCommandLocalization {
    private String giveSuccessful = "Successfully given money to the bank of player";
    private String giveFailed = "Failed to give money to the bank of player";
    private String takeSuccessful = "Successfully taken money from the bank of player";
    private String takeFailed = "Failed to take money from the bank of player";
    private String upgradeSuccessful = "Successfully upgraded the bank level of player";
    private String upgradeFailed = "Could not upgrade the bank of player";
    private String downgradeSuccessful = "Successfully downgraded the bank level of player";
    private String downgradeFailed = "Could not downgrade the bank of player";
    private String bankLevelMinimal = "The bank is at the lowest level";

    public String getGiveSuccessful() {
        return giveSuccessful;
    }

    public String getGiveFailed() {
        return giveFailed;
    }

    public String getTakeSuccessful() {
        return takeSuccessful;
    }

    public String getTakeFailed() {
        return takeFailed;
    }

    public String getUpgradeSuccessful() {
        return upgradeSuccessful;
    }

    public String getUpgradeFailed() {
        return upgradeFailed;
    }

    public String getDowngradeSuccessful() {
        return downgradeSuccessful;
    }

    public String getDowngradeFailed() {
        return downgradeFailed;
    }

    public String getBankLevelMinimal() {
        return bankLevelMinimal;
    }
}
