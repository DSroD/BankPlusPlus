package dez.fortexx.bankplusplus.commands.api.arguments;

import dez.fortexx.bankplusplus.utils.caching.ICachedValue;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class OfflinePlayerArgument implements ICommandArgument<OfflinePlayer> {
    private final String name;
    private final Plugin plugin;
    private final ICachedValue<Set<String>> lowercaseOfflinePlayerNames;

    public OfflinePlayerArgument(String name, Plugin plugin, ICachedValue<Set<String>> lowercaseOfflinePlayerNames) {
        this.name = name;
        this.plugin = plugin;
        this.lowercaseOfflinePlayerNames = lowercaseOfflinePlayerNames;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public OfflinePlayer fromString(String arg) {
        // TODO: find in offline players list
        return plugin.getServer().getOfflinePlayer(arg);
    }

    @Override
    public boolean verifyValue(String arg) {
        final var lcarg = arg.toLowerCase();
        return lowercaseOfflinePlayerNames.get().contains(lcarg);
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String arg) {
        final var lcarg = arg.toLowerCase();
        final var players = lowercaseOfflinePlayerNames.get()
                .stream().filter(s -> s.startsWith(lcarg)).toList();
        if (players.isEmpty()) {
            return null;
        }
        return players;
    }
}
