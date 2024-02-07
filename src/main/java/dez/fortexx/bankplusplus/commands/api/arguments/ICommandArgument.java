package dez.fortexx.bankplusplus.commands.api.arguments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommandArgument<T> {
    String name();
    T fromString(String arg);
    boolean verifyValue(String arg);

    List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String arg);
}
