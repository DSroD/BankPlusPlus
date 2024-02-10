package dez.fortexx.bankplusplus.logging;

import java.util.function.Supplier;

public interface ILogger {
    void debug(String msg);
    void debug(Supplier<String> msg);
    void info(String msg);
    void info(Supplier<String> msg);
    void warn(String msg);
    void warn(Supplier<String> msg);
    void severe(String msg);
    void severe(Supplier<String> msg);

}
