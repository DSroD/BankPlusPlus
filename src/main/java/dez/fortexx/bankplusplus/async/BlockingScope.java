package dez.fortexx.bankplusplus.async;

import java.util.function.Consumer;

public class BlockingScope implements IAsyncScope {
    public static final BlockingScope instance = new BlockingScope();

    @Override
    public void runAsync(Runnable r) {
        r.run();
    }

    @Override
    public void runSync(Runnable r) {
        r.run();
    }

    @Override
    public void runAsync(Consumer<IAsyncScope> r) {
        r.accept(this);
    }

    @Override
    public void runSync(Consumer<IAsyncScope> r) {
        r.accept(this);
    }
}
