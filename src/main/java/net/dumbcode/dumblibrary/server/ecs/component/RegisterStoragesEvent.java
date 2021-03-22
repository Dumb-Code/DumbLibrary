package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.function.Supplier;

public class RegisterStoragesEvent extends Event {

    public <T extends EntityComponent, S extends EntityComponentStorage<T>>EntityComponentType.StorageOverride<T, S> register(EntityComponentType<T, ?> type, String overrideName, Supplier<S> constructor) {
        EntityComponentType.StorageOverride<T, S> storageOverride = new EntityComponentType.StorageOverride<>(overrideName, constructor);
        EntityComponentType.StorageOverride.overrides.computeIfAbsent(type, e -> new HashMap<>()).put(overrideName, storageOverride);
        return storageOverride;
    }

}
