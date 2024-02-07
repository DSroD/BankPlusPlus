package dez.fortexx.bankplusplus.async;

import java.util.function.Consumer;

public interface IAsyncScope {
    void runAsync(Runnable r);
    void runSync(Runnable r);

    void runAsync(Consumer<IAsyncScope> r);
    void runSync(Consumer<IAsyncScope> r);
}
