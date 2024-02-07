package dez.fortexx.bankplusplus.commands.api.result;

import net.md_5.bungee.api.chat.BaseComponent;

public final class BaseComponentResult implements ICommandResult {
    private final BaseComponent[] component;
    public BaseComponentResult(BaseComponent[] c) {
        component = c;
    }

    public BaseComponent[] getComponent() {
        return component;
    }
}
