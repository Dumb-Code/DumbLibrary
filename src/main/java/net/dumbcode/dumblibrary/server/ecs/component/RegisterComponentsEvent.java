package net.dumbcode.dumblibrary.server.ecs.component;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterComponentsEvent extends Event {
    @Getter
    private final IForgeRegistry<EntityComponentType<?, ?>> registry;

    public RegisterComponentsEvent(IForgeRegistry<EntityComponentType<?, ?>> registry) {
        this.registry = registry;
    }
}
