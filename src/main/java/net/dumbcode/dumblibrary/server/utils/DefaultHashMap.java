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
        return this.containsKey(k) ? super.get(k) : this.defaultValue.get();
    }

    public V getOrPut(K k) {
        if(this.containsKey(k)) {
            return this.get(k);
        }
        V v = this.defaultValue.get();
        this.put(k, v);
        return v;
    }
}