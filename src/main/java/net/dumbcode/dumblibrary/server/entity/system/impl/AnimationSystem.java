package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;

public enum AnimationSystem implements EntitySystem {
    INSTANCE;

    private AnimationComponent<?>[] components = null;

    @Override
    public void populateBuffers(EntityManager manager) {
        this.components = manager.resolveFamily(EntityComponentTypes.ANIMATION).populateBuffer(EntityComponentTypes.ANIMATION, this.components);
    }

    @Override
    public void update() {
        for (AnimationComponent<?> component : this.components) {
            component.getFutureAnimations().removeIf(AnimationComponent.FutureAnimation::tick);
        }
    }
}
