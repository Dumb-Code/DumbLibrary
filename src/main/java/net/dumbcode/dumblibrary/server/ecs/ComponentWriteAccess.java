package net.dumbcode.dumblibrary.server.ecs;


import net.dumbcode.dumblibrary.server.ecs.component.*;
import scala.xml.Null;

import javax.annotation.Nullable;

/**
 * The interface used by {@link net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher} to attach
 * components to a component access.
 * @see ComponentMapWriteAccess
 * @author Wyn Price
 */
public interface ComponentWriteAccess extends ComponentAccess {
    /**
     * Attach a component type to this with an optional specified storage.
     * @param type the registered component type to attach
     * @param storage the storage to attach with the {@code type}. If this is null, then the default storage will be used
     * @param <T> the component entry type.
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     */
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, ?> type, @Nullable S storage, @Nullable String storageID) {
        if(storage == null) {
            this.attachComponent(type);
        } else {
            T construct = storage.construct();
            construct.onCreated(this, type, storage, storageID);
            this.attachComponent(type, construct);
        }
    }

    /**
     * Attach a component type to this.
     * @param type the registered component type to attach
     * @param <T> the component entry type.
     * @param <S> the component storage type. Should be {@code ?} if no storage exists.
     */
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, S> type) {
        this.attachComponent(type, type.constructEmpty());
    }

    /**
     * Attach a component to this
     * @param type the registered component type to attach
     * @param component the actual component to attach
     * @param <T> the component entry type.
     */
    <T extends EntityComponent> void attachComponent(EntityComponentType<T, ?> type, T component);
}
