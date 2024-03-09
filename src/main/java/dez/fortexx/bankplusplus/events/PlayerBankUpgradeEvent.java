package dez.fortexx.bankplusplus.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerBankUpgradeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final OfflinePlayer player;
    private final int newLevel;

    public PlayerBankUpgradeEvent(OfflinePlayer player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
