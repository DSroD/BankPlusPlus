package dez.fortexx.bankplusplus.commands.api;

import dez.fortexx.bankplusplus.commands.api.arguments.validator.IArgumentsValidator;
import dez.fortexx.bankplusplus.commands.api.result.*;
import dez.fortexx.bankplusplus.localization.Localization;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandDispatcher implements CommandExecutor, TabCompleter {
    
    private final Map<String, ICommand> commands;
    private final String rootCommandName;
    private final BaseComponent[] missingPermissionsComponent;
    private final Localization localization;
    private final IArgumentsValidator argumentsValidator;

    public CommandDispatcher(String commandName, List<ICommand> subcommands, Localization localization, IArgumentsValidator argumentsValidator) {
        this.rootCommandName = commandName;
        commands = commandMapFromList(
                subcommands
        );
        this.localization = localization;
        missingPermissionsComponent = new ComponentBuilder(localization.getCommandBase().getMissingPermission())
                .color(ChatColor.DARK_RED).create();
        this.argumentsValidator = argumentsValidator;
    }

    public void register(JavaPlugin plugin) {
        final var cmd = plugin.getCommand(this.rootCommandName);
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            String[] args
    ) {
        if (args.length == 0) {
            return sendBaseComponent(commandSender, helpMessage(commandSender));
        }

        final var subcommandName = args[0].toLowerCase();

        if (!commands.containsKey(subcommandName)) {
            return sendBaseComponent(commandSender, helpMessage(commandSender));
        }

        final var subcommand = commands.get(subcommandName);

        final var canInvoke = subcommand.getPermission().map(commandSender::hasPermission).orElse(true);
        if (!canInvoke) {
            return sendBaseComponent(commandSender, missingPermissionsComponent);
        }

        final var subArgs = Arrays.copyOfRange(args, 1, args.length);

        if (!argumentsValidator.validate(subcommand.getCommandArguments(), subArgs)) {
            return handleInvalidUsageResult(commandSender, subcommand);
        }

        final var result = subcommand.invoke(commandSender, subArgs);

        return handleResult(commandSender, subcommand, result);
    }

    private boolean handleResult(
            @NotNull CommandSender commandSender,
            @NotNull ICommand subcommand,
            @NotNull ICommandResult result
    ) {

        if (result instanceof SuccessResult) {
            return true;
        }
        if (result instanceof BaseComponentResult bcr) {
            return sendBaseComponent(commandSender, bcr.getComponent());
        }
        if (result instanceof InvalidCommandSenderResult) {
            return handleInvalidCommandSenderResult(commandSender);
        }
        if (result instanceof InvalidUsageResult) {
            return handleInvalidUsageResult(commandSender, subcommand);
        }
        if (result instanceof MissingPermissionsResult) {
            return sendBaseComponent(commandSender, missingPermissionsComponent);
        }
        if (result instanceof ErrorResult) {
            return handleErrorResult(commandSender);
        }
        return false;
    }

    private boolean handleErrorResult(@NotNull CommandSender commandSender) {
        final var component = new ComponentBuilder(localization.getCommandBase().getError())
                .color(ChatColor.DARK_RED).bold(true)
                .create();
        commandSender.spigot()
                .sendMessage(component);
        return true;
    }

    private boolean handleInvalidCommandSenderResult(@NotNull CommandSender commandSender) {
        final var component = new ComponentBuilder(localization.getCommandBase().getCanNotUse())
                .color(ChatColor.RED).bold(true)
                .create();
        commandSender.spigot()
                .sendMessage(component);
        return true;
    }

    private boolean handleInvalidUsageResult(@NotNull CommandSender commandSender, @NotNull ICommand subcommand) {
        final var cb = new ComponentBuilder(localization.getCommandBase().getInvalidUsage())
                .color(ChatColor.DARK_RED)
                .append(":\n");
        appendCommandHelpLine(subcommand, cb);
        final var component = cb.create();
        commandSender.spigot()
                .sendMessage(component);
        return true;
    }

    private static boolean sendBaseComponent(@NotNull CommandSender commandSender, BaseComponent[] bcr) {
        commandSender.spigot()
                .sendMessage(bcr);
        return true;
    }

    private BaseComponent[] helpMessage(CommandSender cs) {
        final var builder = new ComponentBuilder(localization.getCommandBase().getCommandUsage())
                .color(ChatColor.RED)
                .append(":\n");

        commands.values()
                .stream().filter(c -> c.getPermission()
                        .map(cs::hasPermission)
                        .orElse(true))
                .forEach(
                    c -> appendCommandHelpLine(c, builder)
                            .append("\n")
                );
        return builder.create();
    }

    private ComponentBuilder appendCommandHelpLine(final ICommand c, final ComponentBuilder cb) {
        cb.append("/")
                .color(ChatColor.GOLD).bold(true)
                .append(rootCommandName)
                .append(" ")
                .append(c.getCommandName())
                .append(" ");

        c.getCommandArguments()
                .forEach(a ->
                    cb.append("[")
                            .append(a.name())
                            .append("] ")
                );
        cb.append("- ").color(ChatColor.AQUA)
                .append(c.getCommandDescription()).bold(false);
        return cb;
    }

    private Map<String, ICommand> commandMapFromList(List<ICommand> commands) {
        return commands.stream()
                .collect(Collectors.toMap(
                        ICommand::getCommandName,
                        Function.identity(),
                        (v1, v2) -> v2,
                        // This creates a sorted map
                        TreeMap::new));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final var partialArg = args[0].toLowerCase();
            return commands
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().getPermission().map(commandSender::hasPermission).orElse(true))
                    .map(Map.Entry::getKey)
                    .filter(subCommand -> subCommand.startsWith(partialArg))
                    .collect(Collectors.toList());
        }

        if (args.length > 1) {
            final var currentLength = args.length - 1; // first arg is subcommand, also points to the last arg
            final var subcommand = args[0].toLowerCase();
            final var subcommandInstance = commands.get(subcommand);
            if (subcommandInstance == null)
                return null;

            final var subcommandArgs = subcommandInstance.getCommandArguments();
            // expecting 4 arguments, currentLength is 4 meaning we are completing
            if (subcommandArgs.size() < currentLength)
                return null;

            final var argIdx = currentLength - 1;
            final var argument = subcommandArgs.get(argIdx);
            return argument.tabComplete(commandSender, args[currentLength]);
        }

        return null;
    }
}
