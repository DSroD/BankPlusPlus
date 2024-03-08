package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class AdminCommandLocalization {
    private String giveSuccessful = "Successfully given money to the bank of player";
    private String giveFailed = "Failed to give money to the bank of player";
    private String takeSuccessful = "Successfully taken money from the bank of player";
    private String takeFailed = "Failed to take money from the bank of player";

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
}
