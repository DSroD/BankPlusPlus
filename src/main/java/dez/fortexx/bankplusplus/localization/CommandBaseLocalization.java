package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class CommandBaseLocalization {
    private String commandUsage = "Usage of the command";
    private String canNotUse = "You can not use this command";
    private String invalidUsage = "Invalid usage";
    private String error = "An error occurred when running the command. Please inform administrators about this.";
    private String missingPermission = "You do not have permission to use this command";
    private String playerNotFound = "Player not found!";

    public String getCommandUsage() {
        return commandUsage;
    }

    public String getCanNotUse() {
        return canNotUse;
    }

    public String getInvalidUsage() {
        return invalidUsage;
    }

    public String getError() {
        return error;
    }

    public String getMissingPermission() {
        return missingPermission;
    }

    public String getPlayerNotFound() {
        return playerNotFound;
    }
}
