package dez.fortexx.bankplusplus.bank.balance;

import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.FailureEconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.SuccessEconomyResult;
import dez.fortexx.bankplusplus.persistence.IBankStore;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

/**
 * EconomyManager proxy over bank store
 */
public class BankEconomyManager implements IBankEconomyManager {
    private final IBankStore bankStore;

    public BankEconomyManager(IBankStore bankStore) {
        this.bankStore = bankStore;
    }

    @Override
    public EconomyResult deposit(OfflinePlayer player, BigDecimal amount) {
        if (bankStore.addBankFunds(player.getUniqueId(), amount))
            return SuccessEconomyResult.instance;
        return FailureEconomyResult.instance;
    }

    @Override
    public EconomyResult withdraw(OfflinePlayer player, BigDecimal amount) {
        if (bankStore.takeBankFunds(player.getUniqueId(), amount))
            return SuccessEconomyResult.instance;
        return FailureEconomyResult.instance;
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return bankStore.getBankFunds(player.getUniqueId());
    }

    @Override
    public boolean upgradeLevel(OfflinePlayer player) {
        return bankStore.upgradeLevel(player.getUniqueId());
    }

    @Override
    public int getBankLevel(OfflinePlayer player) {
        return bankStore.getBankLevel(player.getUniqueId());
    }
}
