package dez.fortexx.bankplusplus.commands.api.arguments.validator;

import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;

import java.util.List;

public interface IArgumentsValidator {
    public boolean validate(List<ICommandArgument<?>> argumentDefinitions, String[] arguments);
}
