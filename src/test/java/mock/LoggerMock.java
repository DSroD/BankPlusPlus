package mock;

import dez.fortexx.bankplusplus.logging.ILogger;

import java.util.function.Supplier;

public class LoggerMock implements ILogger {
    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(Supplier<String> msg) {

    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(Supplier<String> msg) {

    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public void warn(Supplier<String> msg) {

    }

    @Override
    public void severe(String msg) {

    }

    @Override
    public void severe(Supplier<String> msg) {

    }
}
