package net.dumbcode.dumblibrary.server.ecs.component;

import com.google.common.collect.Maps;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class EntityComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> extends ForgeRegistryEntry<EntityComponentType<?, ?>> implements Function<ComponentAccess, Optional<T>> {
    @Nonnull
    public abstract T constructEmpty();

    @Nullable
    public abstract S constructStorage();

    @Nonnull
    public abstract ResourceLocation getIdentifier();

    @Nonnull
    public abstract Class<? extends T> getType();

    public abstract boolean defaultAttach();

    @SuppressWarnings("unchecked")
    public static Class<EntityComponentType<?, ?>> getWildcardType() {
        return (Class<EntityComponentType<?, ?>>) (Class<?>) EntityComponentType.class;
    }

    @Value
    public static class StorageOverride<T extends EntityComponent, S extends EntityComponentStorage<T>> {

        private static final List<StorageOverrideNeedingAdding<?, ?>> needAdding = new ArrayList<>();
        public static Map<EntityComponentType<?, ?>, Map<String, StorageOverride<?, ?>>> overrides = Maps.newHashMap();

        String storageID;
        Supplier<S> storage;

        public EntityComponentStorage<?> construct() {
            return this.storage.get();
        }
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> StorageOverride<T, S> registerStorageOverride(
        Supplier<? extends EntityComponentType<T, ?>> type, String overrideName, Supplier<S> constructor
    ) {
        StorageOverride<T, S> storageOverride = new StorageOverride<>(overrideName, constructor);
        StorageOverride.needAdding.add(new StorageOverrideNeedingAdding<>(type, storageOverride));
        return storageOverride;
    }

    public static void addAll() {
        for (StorageOverrideNeedingAdding<?, ?> needingAdding : StorageOverride.needAdding) {
            needingAdding.add();
        }
    }

    //Used so you can do Optional<ComponentAccess>.flatMap(componentType)...
    @Override
    public Optional<T> apply(ComponentAccess componentAccess) {
        return componentAccess.get(this);
    }

    @Value
    private static class StorageOverrideNeedingAdding<T extends EntityComponent, S extends EntityComponentStorage<T>> {
        Supplier<? extends EntityComponentType<T, ?>> type;
        StorageOverride<T, S> storageOverride;

        public void add() {
            StorageOverride.overrides.computeIfAbsent(type.get(), e -> new HashMap<>()).put(storageOverride.getStorageID(), storageOverride);
        }
    }
}
