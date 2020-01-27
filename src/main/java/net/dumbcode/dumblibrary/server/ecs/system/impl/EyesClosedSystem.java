package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.world.World;

public enum EyesClosedSystem implements EntitySystem {
    INSTANCE;

    private EyesClosedComponent[] components = new EyesClosedComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        this.components = manager.resolveFamily(EntityComponentTypes.EYES_CLOSED).populateBuffer(EntityComponentTypes.EYES_CLOSED, this.components);
    }

    @Override
    public void update(World world) {
        for (EyesClosedComponent component : this.components) {
            if(component.getBlinkTicksLeft() > 0) {
                component.setBlinkTicksLeft(component.getBlinkTicksLeft() - 1);
            }
        }
    }
}
