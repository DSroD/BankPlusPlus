package dez.fortexx.bankplusplus.async;

import java.util.function.*;

public class AsyncTask<T> {
    private final Function<IAsyncScope, T> task;

    private AsyncTask(Supplier<T> task) {
        this.task = (s) -> task.get();
    }

    public static <T> AsyncTask<T> of(Supplier<T> task) {
        return new AsyncTask<>(task);
    }

    private AsyncTask(Function<IAsyncScope, T> task) {
        this.task = task;
    }

    public static <T> AsyncTask<T> of(Function<IAsyncScope, T> task) {
        return new AsyncTask<>(task);
    }

    /**
     * Runs task asynchronously in given scope
     */
    public void runInScope(IAsyncScope scope) {
        scope.runAsync(() -> task.apply(scope));
    }

    public <R> AsyncTask<R> then(Function<T, R> fn) {
        final var newFn = task.andThen(fn);
        return new AsyncTask<>(newFn);
    }

    public <R> AsyncTask<R> then(BiFunction<IAsyncScope, T, R> fn) {
        final Function<IAsyncScope, R> newFn = (s) -> {
            final var result = task.apply(s);
            return fn.apply(s, result);
        };
        return new AsyncTask<>(newFn);
    }

    public AsyncTask<Void> then(Consumer<T> fn) {
        final Function<IAsyncScope, Void> newFn = task.andThen(val -> {
            fn.accept(val);
            return null;
        });
        return new AsyncTask<>(newFn);
    }

    public AsyncTask<Void> then(BiConsumer<IAsyncScope, T> fn) {
        final Function<IAsyncScope,Void> newFn = (s) -> {
            final var result = task.apply(s);
            fn.accept(s, result);
            return null;
        };
        return new AsyncTask<>(newFn);
    }
}
