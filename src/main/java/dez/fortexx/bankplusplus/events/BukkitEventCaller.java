package dez.fortexx.bankplusplus.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class BukkitEventCaller implements IEventCaller {
    @Override
    public void call(Event e) {
        Bukkit.getPluginManager().callEvent(e);
    }
}
