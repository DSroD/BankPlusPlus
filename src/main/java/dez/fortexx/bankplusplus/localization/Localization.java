package dez.fortexx.bankplusplus.localization;

import de.exlll.configlib.Configuration;
import dez.fortexx.bankplusplus.configuration.configurator.FromFile;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
@FromFile("localization.yml")
public class Localization {
    private String newBalance = "New balance";
    private String fees = "Fees";
    private String amount = "Amount";
    private String player = "Player";
    private String balance = "Account balance";
    private String depositSuccessful = "Successfully deposited money to the bank";
    private String depositFailed = "Failed to deposit money to the bank";
    private String withdrawSuccessful = "Successfully taken money from the bank";
    private String withdrawFailed = "Failed to withdraw money from the bank";
    private String insufficientFunds = "Insufficient funds";
    private String maximumWithdrawalPossible = "Maximal withdrawal possible";
    private String amountTooSmall = "Transaction amount too small";
    private String limitViolation = "Transaction violates your bank limit";
    private String upgradeSuccessful = "Bank upgraded successfully";
    private String upgradeFailed = "Could not upgrade bank";
    private String bankAlreadyMaxLevel = "Bank is already fully upgraded";
    private String missingRequirements = "Missing requirements for the upgrade";
    private String newBalanceLimitIs = "Your new bank balance limit is";
    private String balanceLimit = "Maximal balance";
    private String accountLevelName = "Account level";
    private String upgradeAvailable = "Upgrade is available";
    private String nextLevel = "Next level";
    private String nextLevelBalanceLimit = "Next level balance limit";
    private String nextLevelRequirements = "Next level requirements";

    private String currencySymbol = "$";
    private boolean currencySymbolBeforeNumber = true;

    private CommandDescriptionLocalization commandDescriptions = new CommandDescriptionLocalization();
    private CommandBaseLocalization commandBase = new CommandBaseLocalization();

    public String getNewBalance() {
        return newBalance;
    }
    public String getFees() {
        return fees;
    }
    public String getAmount() {
        return amount;
    }

    public CommandDescriptionLocalization getCommandDescriptions() {
        return commandDescriptions;
    }

    public String getBalance() {
        return balance;
    }

    public String getDepositSuccessful() {
        return depositSuccessful;
    }

    public String getWithdrawSuccessful() {
        return withdrawSuccessful;
    }

    public CommandBaseLocalization getCommandBase() {
        return commandBase;
    }

    public String getDepositFailed() {
        return depositFailed;
    }

    public String getWithdrawFailed() {
        return withdrawFailed;
    }

    public String getInsufficientFunds() {
        return insufficientFunds;
    }

    public String getAmountTooSmall() {
        return amountTooSmall;
    }

    public String getLimitViolation() {
        return limitViolation;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public boolean isCurrencySymbolBeforeNumber() {
        return currencySymbolBeforeNumber;
    }

    public String getUpgradeSuccessful() {
        return upgradeSuccessful;
    }

    public String getUpgradeFailed() {
        return upgradeFailed;
    }

    public String getNewBalanceLimitIs() {
        return newBalanceLimitIs;
    }

    public String getBankAlreadyMaxLevel() {
        return bankAlreadyMaxLevel;
    }

    public String getMissingRequirements() {
        return missingRequirements;
    }

    public String getBalanceLimit() {
        return balanceLimit;
    }

    public String getAccountLevelName() {
        return accountLevelName;
    }

    public String getNextLevel() {
        return nextLevel;
    }

    public String getNextLevelBalanceLimit() {
        return nextLevelBalanceLimit;
    }

    public String getNextLevelRequirements() {
        return nextLevelRequirements;
    }

    public String getUpgradeAvailable() {
        return upgradeAvailable;
    }

    public String getMaximumWithdrawalPossible() {
        return maximumWithdrawalPossible;
    }

    public String getPlayer() {
        return player;
    }
}
