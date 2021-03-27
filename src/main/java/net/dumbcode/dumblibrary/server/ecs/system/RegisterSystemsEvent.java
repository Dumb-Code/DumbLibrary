package net.dumbcode.dumblibrary.server.ecs.system;

import lombok.Getter;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class RegisterSystemsEvent extends Event {
    @Getter
    private final World world;
    private final List<EntitySystem> systems;

    public RegisterSystemsEvent(World world, List<EntitySystem> systems) {
        this.world = world;
        this.systems = systems;
    }

    public void registerSystem(EntitySystem system) {
        this.systems.add(system);
    }
}
