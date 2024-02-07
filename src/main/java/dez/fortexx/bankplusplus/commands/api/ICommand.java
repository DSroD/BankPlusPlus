package dez.fortexx.bankplusplus.commands.api;

import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;
import dez.fortexx.bankplusplus.commands.api.result.ICommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface ICommand {
    @NotNull String getCommandName();
    @NotNull String getCommandDescription();
    @NotNull List<ICommandArgument<?>> getCommandArguments();
    @NotNull Optional<String> getPermission();
    @NotNull ICommandResult invoke(CommandSender sender, String[] args);
}
