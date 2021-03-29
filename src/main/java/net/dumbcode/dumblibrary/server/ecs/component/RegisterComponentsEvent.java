package net.dumbcode.dumblibrary.server.ecs.component;

import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterComponentsEvent extends Event implements IModBusEvent {
    @Getter
    private final IForgeRegistry<EntityComponentType<?, ?>> registry;

    public RegisterComponentsEvent(IForgeRegistry<EntityComponentType<?, ?>> registry) {
        this.registry = registry;
    }
}
