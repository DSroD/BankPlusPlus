package dez.fortexx.bankplusplus.commands.api.arguments;

import dez.fortexx.bankplusplus.utils.ITimeProvider;
import dez.fortexx.bankplusplus.utils.caching.ICachedValue;
import dez.fortexx.bankplusplus.utils.caching.TTLCachedValue;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OfflinePlayerArgument implements ICommandArgument<OfflinePlayer> {
    private final String name;
    private final Plugin plugin;
    private final ICachedValue<Set<String>> lowercaseOfflinePlayerNames;

    public OfflinePlayerArgument(
            String name,
            Plugin plugin,
            ITimeProvider timeProvider
    ) {
        this.name = name;
        this.plugin = plugin;
        this.lowercaseOfflinePlayerNames = new TTLCachedValue<>(
                () -> Arrays.stream(plugin.getServer().getOfflinePlayers())
                        .map(OfflinePlayer::getName).collect(Collectors.toSet()),
                Duration.ofSeconds(20),
                timeProvider
        );
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @Nullable OfflinePlayer fromString(String arg) {
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .filter(o -> o.getName().equalsIgnoreCase(arg)).findFirst()
                .orElse(null);
    }

    @Override
    public boolean verifyValue(String arg) {
        return Arrays.stream(plugin.getServer().getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .anyMatch(o -> o.equalsIgnoreCase(arg));
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String arg) {
        final var lcarg = arg.toLowerCase();
        final var players = lowercaseOfflinePlayerNames.get()
                .stream().filter(s -> s.startsWith(lcarg)).limit(8).toList();
        if (players.isEmpty()) {
            return null;
        }
        return players;
    }
}
