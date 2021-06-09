package net.dumbcode.dumblibrary.server.ecs.component;

import com.google.common.collect.Maps;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface EntityComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> extends IForgeRegistryEntry<EntityComponentType<?, ?>>, Function<ComponentAccess, Optional<T>> {
    @Nonnull
    T constructEmpty();

    @Nullable
    S constructStorage();

    @Nonnull
    ResourceLocation getIdentifier();

    @Nonnull
    Class<? extends T> getType();

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

        public static Map<EntityComponentType<?, ?>, Map<String, StorageOverride<?, ?>>> overrides = Maps.newHashMap();

        String storageID;
        Supplier<S> storage;

        public EntityComponentStorage<?> construct() {
            return this.storage.get();
        }
    }

    //Used so you can do Optional<ComponentAccess>.flatMap(componentType)...
    @Override
    default Optional<T> apply(ComponentAccess componentAccess) {
        return componentAccess.get(this);
    }
}
