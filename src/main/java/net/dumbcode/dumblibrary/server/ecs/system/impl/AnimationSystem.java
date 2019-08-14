package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.world.World;

public enum AnimationSystem implements EntitySystem {
    INSTANCE;

    private AnimationComponent<?>[] components = null;

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        this.components = manager.resolveFamily(EntityComponentTypes.ANIMATION).populateBuffer(EntityComponentTypes.ANIMATION, this.components);
    }

    @Override
    public void update(World world) {
        for (AnimationComponent<?> component : this.components) {
            component.getFutureAnimations().removeIf(AnimationComponent.FutureAnimation::tick);
        }
    }
}
