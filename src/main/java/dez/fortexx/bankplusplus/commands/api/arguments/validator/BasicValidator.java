package dez.fortexx.bankplusplus.commands.api.arguments.validator;

import dez.fortexx.bankplusplus.commands.api.arguments.ICommandArgument;

import java.util.List;

public class BasicValidator implements IArgumentsValidator {
    @Override
    public boolean validate(List<ICommandArgument<?>> argumentDefinitions, String[] arguments) {
        if (arguments.length != argumentDefinitions.size())
            return false;

        for (var i = 0; i < arguments.length; i++) {
            final var argument = arguments[i];
            final var definition = argumentDefinitions.get(i);
            if (!definition.verifyValue(argument))
                return false;
        }

        return true;
    }
}
