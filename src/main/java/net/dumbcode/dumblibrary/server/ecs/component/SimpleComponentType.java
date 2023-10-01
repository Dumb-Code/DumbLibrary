package net.dumbcode.dumblibrary.server.ecs.component;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@ToString
public class SimpleComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> extends EntityComponentType<T, S> {
    @ToString.Exclude private final Supplier<T> constructor;
    @Nullable @ToString.Exclude private final Supplier<S> storageConstructor;
    private final boolean defaultAttach;
    private final Class<T> type;

    private SimpleComponentType(Supplier<T> constructor, @Nullable Supplier<S> storageConstructor, boolean defaultAttach, Class<T> type) {
        this.constructor = constructor;
        this.defaultAttach = defaultAttach;
        this.type = type;
        this.storageConstructor = storageConstructor;
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> EntityComponentType<T, S> of(Class<T> type, Supplier<T> constructor) {
        return of(type, constructor, null);
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> EntityComponentType<T, S> of(Class<T> type, Supplier<T> constructor, boolean defaultAttach) {
        return of(type, constructor, null, defaultAttach);
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> EntityComponentType<T, S> of(Class<T> type, Supplier<T> constructor, @Nullable Supplier<S> storage) {
        return of(type, constructor, storage, true);
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> EntityComponentType<T, S> of(Class<T> type, Supplier<T> constructor, @Nullable Supplier<S> storage, boolean defaultAttach) {
        return new SimpleComponentType<>(constructor, storage, defaultAttach, type);
    }


    @Nullable
    @Override
    public S constructStorage() {
        return this.storageConstructor == null ? null : this.storageConstructor.get();
    }

    @Nonnull
    @Override
    public T constructEmpty() {
        return this.constructor.get();
    }

    @Nonnull
    @Override
    public ResourceLocation getIdentifier() {
        return this.getRegistryName();
    }

    @Nonnull
    @Override
    public Class<? extends T> getType() {
        return this.type;
    }

    @Nonnull
    @Override
    public boolean defaultAttach() {
        return this.defaultAttach;
    }
}
