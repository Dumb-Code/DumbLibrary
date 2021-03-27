package net.dumbcode.dumblibrary.server.ecs.component;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SimpleComponentType<T extends EntityComponent, S extends EntityComponentStorage<T>> implements EntityComponentType<T, S> {
    @ToString.Exclude private final Supplier<T> constructor;
    @Nullable @ToString.Exclude private final Supplier<S> storageConstructor;
    @EqualsAndHashCode.Include private final ResourceLocation identifier;
    @EqualsAndHashCode.Include private final boolean defaultAttach;
    private final Class<T> type;

    private SimpleComponentType(Supplier<T> constructor, @Nullable Supplier<S> storageConstructor, ResourceLocation identifier, boolean defaultAttach, Class<T> type) {
        this.constructor = constructor;
        this.identifier = identifier;
        this.defaultAttach = defaultAttach;
        this.type = type;
        this.storageConstructor = storageConstructor;
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> Builder<T, S> builder(Class<T> type) {
        return new Builder<>(type, null);
    }

    public static <T extends EntityComponent, S extends EntityComponentStorage<T>> Builder<T, S> builder(Class<T> type, Class<S> storageType) {
        return new Builder<>(type, storageType);
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
        return this.identifier;
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


    public static class Builder<T extends EntityComponent, S extends EntityComponentStorage<T>> {
        private final Class<T> type;
        private final Class<S> storageType; //Used to infer types
        private Supplier<T> constructor;
        private Supplier<S> storageConstructor;
        private ResourceLocation identifier;
        private boolean defaultAttach = true;

        private Builder(Class<T> type, Class<S> storageType) {
            this.type = type;
            this.storageType = storageType;
        }

        public Builder<T, S> withConstructor(Supplier<T> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder<T, S> withStorage(Supplier<S> storageConstructor) {
            this.storageConstructor = storageConstructor;
            return this;
        }

        public Builder<T, S> withIdentifier(ResourceLocation identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder<T, S> disableDefaultAttach() {
            this.defaultAttach = false;
            return this;
        }

        public EntityComponentType<T, S> build() {
            Preconditions.checkNotNull(this.identifier, "Component identifier must be set");
            if(this.constructor == null) {
                DumbLibrary.getLogger().warn("No constructor set, trying to set to empty constructor of type {}", this.type.getName());
                try {
                    Constructor<T> cons = this.type.getConstructor();
                    this.constructor = () -> {
                        try {
                            return cons.newInstance();
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new IllegalStateException("Unable to construct component of class " + this.type.getName() + ", with component type " + this.identifier, e);
                        }
                    };
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Unable to create empty constructor for " + this.type.getSimpleName(), e);
                }

            }
            return new SimpleComponentType<>(this.constructor, this.storageConstructor, this.identifier, this.defaultAttach, this.type);
        }
    }
}
