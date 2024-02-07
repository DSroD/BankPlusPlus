package dez.fortexx.bankplusplus.vault;

import dez.fortexx.bankplusplus.api.economy.EconomyResult;
import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.api.economy.transaction.DescribedTransactionFailureResult;
import dez.fortexx.bankplusplus.api.economy.transaction.ITransactionResult;
import dez.fortexx.bankplusplus.api.economy.transaction.SuccessTransactionResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

public class VaultEconomy implements IBalanceManager {
    private final Economy vaultEconomy;

    public VaultEconomy(Economy vaultEconomy) {
        this.vaultEconomy = vaultEconomy;
    }

    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return BigDecimal.valueOf(vaultEconomy.getBalance(player));
    }

    @Override
    public ITransactionResult deposit(OfflinePlayer player, BigDecimal amount) {
        final var result = vaultEconomy.depositPlayer(player, amount.doubleValue());
        if (result.type == EconomyResponse.ResponseType.SUCCESS)
            return new SuccessTransactionResult(new BigDecimal(result.amount), BigDecimal.ZERO);

        return new DescribedTransactionFailureResult(result.errorMessage);
    }

    @Override
    public ITransactionResult withdraw(OfflinePlayer player, BigDecimal amount) {
        final var result = vaultEconomy.withdrawPlayer(player, amount.doubleValue());
        if (result.type == EconomyResponse.ResponseType.SUCCESS)
            return new SuccessTransactionResult(new BigDecimal(result.amount), BigDecimal.ZERO);

        return new DescribedTransactionFailureResult(result.errorMessage);
    }
}
