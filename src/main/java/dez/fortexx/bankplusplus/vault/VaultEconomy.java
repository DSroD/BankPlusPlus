package dez.fortexx.bankplusplus.vault;

import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.api.economy.result.DescribedFailure;
import dez.fortexx.bankplusplus.api.economy.result.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.result.Success;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class VaultEconomy implements IEconomyManager {
    private final Economy vaultEconomy;

    public VaultEconomy(Economy vaultEconomy) {
        this.vaultEconomy = vaultEconomy;
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(vaultEconomy.getBalance(player));
    }

    @Override
    public EconomyResult deposit(OfflinePlayer player, BigDecimal amount) {
        final var result = vaultEconomy.depositPlayer(player, amount.doubleValue());
        if (result.type == EconomyResponse.ResponseType.SUCCESS)
            return new Success(BigDecimal.valueOf(result.balance), BigDecimal.ZERO);

        return new DescribedFailure(result.errorMessage);
    }

    @Override
    public EconomyResult withdraw(OfflinePlayer player, BigDecimal amount) {
        final var result = vaultEconomy.withdrawPlayer(player, amount.doubleValue());
        if (result.type == EconomyResponse.ResponseType.SUCCESS)
            return new Success(BigDecimal.valueOf(result.balance), BigDecimal.ZERO);

        return new DescribedFailure(result.errorMessage);
    }
}
