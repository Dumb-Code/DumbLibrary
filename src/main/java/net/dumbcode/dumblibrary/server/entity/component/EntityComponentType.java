package net.dumbcode.dumblibrary.server.entity.component;

import com.google.common.collect.Maps;
import lombok.Value;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public interface EntityComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> extends IForgeRegistryEntry<EntityComponentType<?, ?>> {
    @Nonnull
    T constructEmpty();

    @Nullable
    S constructStorage();

    @Nonnull
    ResourceLocation getIdentifier();

    @Nonnull
    Class<? extends T> getType();

    @Nonnull
    boolean defaultAttach();

    @Override
    default Class<EntityComponentType<?,?>> getRegistryType() {
        return getWildcardType();
    }

    @Override
    default EntityComponentType<?, ?> setRegistryName(ResourceLocation name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    default ResourceLocation getRegistryName() {
        return this.getIdentifier();
    }

    @SuppressWarnings("unchecked")
    static Class<EntityComponentType<?, ?>> getWildcardType() {
        return (Class<EntityComponentType<?, ?>>) (Class<?>) EntityComponentType.class;
    }

    @Value
    class StorageOverride<T extends EntityComponent, S extends EntityComponentStorage<T>> {

        public static Map<EntityComponentType<?, ?>, Map<String, StorageOverride>> overrides = Maps.newHashMap();

        String storageID;
        Supplier<S> storage;

        public EntityComponentStorage<?> construct() {
            return this.storage.get();
        }
    }
}
