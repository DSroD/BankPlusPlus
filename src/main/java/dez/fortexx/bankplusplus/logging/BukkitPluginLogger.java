package dez.fortexx.bankplusplus.logging;

import dez.fortexx.bankplusplus.BankPlusPlus;
import dez.fortexx.bankplusplus.configuration.LogLevel;

import java.util.function.Supplier;
import java.util.logging.Level;

public class BukkitPluginLogger implements ILogger {
    private final BankPlusPlus plugin;

    public BukkitPluginLogger(BankPlusPlus plugin, LogLevel logLevel) {
        this.plugin = plugin;
        final var level = switch (logLevel) {
            case DEBUG ->  Level.ALL;
            case INFO -> Level.INFO;
            case WARNING -> Level.WARNING;
            case SEVERE -> Level.SEVERE;
        };
        plugin.getLogger().setLevel(level);
    }

    @Override
    public void debug(String msg) {
        plugin.getLogger().finer(msg);
    }

    @Override
    public void debug(Supplier<String> msg) {
        plugin.getLogger().finer(msg);
    }

    @Override
    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void info(Supplier<String> msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

    @Override
    public void warn(Supplier<String> msg) {
        plugin.getLogger().warning(msg);
    }

    @Override
    public void severe(String msg) {
        plugin.getLogger().severe(msg);
    }

    @Override
    public void severe(Supplier<String> msg) {
        plugin.getLogger().severe(msg);
    }
}
