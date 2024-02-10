package dez.fortexx.bankplusplus.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class BukkitEventDispatcher implements IEventDispatcher {
    @Override
    public void dispatch(Event e) {
        Bukkit.getPluginManager().callEvent(e);
    }
}
