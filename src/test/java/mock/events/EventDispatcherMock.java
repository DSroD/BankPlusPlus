package mock.events;

import dez.fortexx.bankplusplus.events.IEventDispatcher;
import org.bukkit.event.Event;

import java.util.HashSet;
import java.util.Set;

public class EventDispatcherMock implements IEventDispatcher {

    private final Set<Class<? extends Event>> calledWith = new HashSet<>();
    @Override
    public void dispatch(Event e) {
        calledWith.add(e.getClass());
    }

    public boolean wasCalledWith(Class<? extends Event> cls) {
        return calledWith.contains(cls);
    }
}
