package net.dumbcode.dumblibrary.server.entity;


import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentType;

import javax.annotation.Nullable;

public interface ComponentWriteAccess extends ComponentAccess {
    default <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, S> type, @Nullable S storage) {
        if(storage == null) {
            this.attachComponent(type);
        } else {
            this.attachComponent(type, storage.construct());
        }
    }

    default <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, S> type) {
        this.attachComponent(type, type.constructEmpty());
    }

    <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, S> type, T component);
}
