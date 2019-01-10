package net.dumbcode.dumblibrary.server.utils;

import lombok.NonNull;

import java.util.HashMap;
import java.util.function.Supplier;

public class DefaultHashMap<K,V> extends HashMap<K,V> {
    protected Supplier<V> defaultValue;
    public DefaultHashMap(@NonNull Supplier<V> defaultValue) {
        this.defaultValue = defaultValue;
    }
    @Override
    public V get(Object k) {
        return containsKey(k) ? super.get(k) : this.defaultValue.get();
    }
}