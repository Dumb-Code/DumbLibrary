package net.dumbcode.dumblibrary.server.ecs;


import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

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
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     * @return the component attached to the type {@code type}, or null if there was no component of that type attached.
     */
    @Nullable
    <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type);

    /**
     * Gets the specified component, throwing an exception if it cannot be found.
     * @param type the registered component type to get the component from.
     * @param <T> the component entry type.
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     * @return the component attached to the type {@code type}
     * @throws ComponentNotFoundException If there is no component attached to the type {@code type}
     */
    @Nonnull
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrExcept(EntityComponentType<T, S> type) throws ComponentNotFoundException {
        T component = this.getOrNull(type);
        if (component == null) {
            throw new ComponentNotFoundException(this, type);
        }
        return component;
    }

    /**
     * Gets an optional of the specified component
     * @param type the registered component type to get the component from.
     * @param <T> the component entry type.
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     * @return an optional of the component attached to the type {@code type}. If empty there is no component attached to the type {@code type}.
     */
    @Nonnull
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> Optional<T> get(EntityComponentType<T, S> type) {
        return Optional.ofNullable(this.getOrNull(type));
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
            if (component instanceof FinalizableComponent) {
                FinalizableComponent aiComponent = (FinalizableComponent) component;
                aiComponent.finalizeComponent(this);
            }
        }
    }
}
