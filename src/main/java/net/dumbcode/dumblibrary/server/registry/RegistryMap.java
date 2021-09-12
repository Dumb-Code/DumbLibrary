package net.dumbcode.dumblibrary.server.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegistryMap<K, V extends IForgeRegistryEntry<? super V>> {

    private final Map<K, RegistryObject<V>> map = new HashMap<>();

    public void putRegistry(K key, RegistryObject<V> value) {
        this.map.put(key, value);
    }

    public V get(K key) {
        return this.map.get(key).get();
    }

    public boolean containsValue(V value) {
        for (RegistryObject<V> v : this.map.values()) {
            if (v.get() == value) {
                return true;
            }
        }
        return false;
    }

    public Collection<V> values() {
        List<V> list = new ArrayList<>();
        for (RegistryObject<V> object : this.map.values()) {
            list.add(object.get());
        }
        return list;
    }
}
