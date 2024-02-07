package dez.fortexx.bankplusplus.commands.api.arguments;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class BigDecimalArgument implements ICommandArgument<BigDecimal> {
    private final String name;
    private static final List<String> BLANK_TAB_COMPLETE = List.of(
            "1000",
            "5000",
            "10000",
            "50000",
            "10000"
    );

    public BigDecimalArgument(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public BigDecimal fromString(String arg) {
        return new BigDecimal(arg);
    }

    @Override
    public boolean verifyValue(String arg) {
        return NumberUtils.isNumber(arg);
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String arg) {
        if (arg.isBlank())
            return BLANK_TAB_COMPLETE;

        if (!NumberUtils.isNumber(arg))
            return null;
        // Don't complement decimal numbers
        if (arg.contains("."))
            return List.of();
        return List.of(
                arg + "0",
                arg + "00",
                arg + "000"
        );
    }
}
