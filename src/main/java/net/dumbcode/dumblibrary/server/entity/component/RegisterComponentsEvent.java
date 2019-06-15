package net.dumbcode.dumblibrary.server.entity.component;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentType;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

public class RegisterComponentsEvent extends Event {
    @Getter
    private final IForgeRegistry<EntityComponentType<?, ?>> registry;

    public RegisterComponentsEvent(IForgeRegistry<EntityComponentType<?, ?>> registry) {
        this.registry = registry;
    }
}
