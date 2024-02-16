package dez.fortexx.bankplusplus.utils.caching;

public interface ICachedValue<T> {
    T get();
    void invalidate();
}
