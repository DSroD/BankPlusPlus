package dez.fortexx.bankplusplus.events;

import dez.fortexx.bankplusplus.api.banktransactions.TransactionType;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;

public class PlayerBankTransactionEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer player;
    private final BigDecimal amount;
    private final BigDecimal tax;
    private final TransactionType transactionType;

    public PlayerBankTransactionEvent(
            OfflinePlayer player,
            BigDecimal amount,
            BigDecimal tax,
            TransactionType transactionType
    ) {
        this.player = player;
        this.amount = amount;
        this.tax = tax;
        this.transactionType = transactionType;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Amount after tax deduction
     */
    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Total tax deduction
     */
    public BigDecimal getTax() {
        return tax;
    }
}
