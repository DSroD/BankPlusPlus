package dez.fortexx.bankplusplus.events;

import org.bukkit.event.Event;

@FunctionalInterface
public interface IEventDispatcher {
    void dispatch(Event e);
}
