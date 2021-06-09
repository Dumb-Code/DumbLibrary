package net.dumbcode.dumblibrary.server.ecs;


import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The base interface for accessing the components. This is implemented by ALL objects wanting to use the ecs model.
 * @see ComponentWriteAccess
 * @see ComponentMapWriteAccess
 * @author gegy1000
 * @author Wyn Price
 */
public interface ComponentAccess {
    /**
     * Gets the specified component, returns null if it cannot be found.
     * @param type the registered component type to get the component from.
     * @param <T> the component entry type.
     * @return the component attached to the type {@code type}, or null if there was no component of that type attached.
     */
    @Nullable
    <T extends EntityComponent> T getOrNull(EntityComponentType<T, ?> type);
    default <T extends EntityComponent> T getOrNull(Supplier<? extends EntityComponentType<T, ?>> supplier) {
        return this.getOrNull(supplier.get());
    }

    /**
     * Gets the specified component, throwing an exception if it cannot be found.
     * @param type the registered component type to get the component from.
     * @param <T> the component entry type.
     * @return the component attached to the type {@code type}
     * @throws ComponentNotFoundException If there is no component attached to the type {@code type}
     */
    @Nonnull
    default <T extends EntityComponent> T getOrExcept(EntityComponentType<T, ?> type) throws ComponentNotFoundException {
        T component = this.getOrNull(type);
        if (component == null) {
            throw new ComponentNotFoundException(this, type);
        }
        return component;
    }
    @Nonnull
    default <T extends EntityComponent> T getOrExcept(Supplier<? extends EntityComponentType<T, ?>> supplier) throws ComponentNotFoundException {
        return this.getOrExcept(supplier.get());
    }

    /**
     * Gets an optional of the specified component
     * @param type the registered component type to get the component from.
     * @param <T> the component entry type.
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     * @return an optional of the component attached to the type {@code type}. If empty there is no component attached to the type {@code type}.
     */
    @Nonnull
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> Optional<T> get(EntityComponentType<T, ? extends S> type) {
        return Optional.ofNullable(this.getOrNull(type));
    }
    @Nonnull
    default <T extends EntityComponent> Optional<T> get(Supplier<? extends EntityComponentType<T, ?>> supplier) {
        return Optional.ofNullable(this.getOrNull(supplier.get()));
    }

    /**
     * Get's all of the components
     * @return all of the current attached components.
     */
    @Nonnull
    Collection<EntityComponent> getAllComponents();

    /**
     * Returns true if this component contains the specified component
     * @param type the registered component type to test if this contains it.
     * @return true if this does contain it, false otherwise.
     */
    default boolean contains(EntityComponentType<?, ?> type) {
        return this.getOrNull(type) != null;
    }
    default <E extends EntityComponentType<?, ?>> boolean contains(Supplier<E> supplier) {
        return this.contains(supplier.get());
    }

    /**
     * Returns true if this component contains all of the specified components.
     * @param types the registered component types to test if this contains them.
     * @return true if this does contain them, false otherwise.
     */
    default boolean matchesAll(EntityComponentType<?, ?>... types) {
        for (EntityComponentType<?, ?> type : types) {
            if (!this.contains(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Should be called once all components are added. This finalizes all the components
     * allowing them to interact with each other.
     */
    default void finalizeComponents() {
        for (EntityComponent component : this.getAllComponents()) {
            component.setResync(this);
        }
        for (EntityComponent component : Lists.newArrayList(this.getAllComponents())) {
            if(component instanceof FinalizableAdditiveComponent) {
                ((FinalizableAdditiveComponent) component).finalizeAdditiveComponent(this);
            }
        }
        for (EntityComponent component : this.getAllComponents()) {
            if (component instanceof FinalizableComponent) {
                FinalizableComponent aiComponent = (FinalizableComponent) component;
                aiComponent.finalizeComponent(this);
            }
        }
    }
}
