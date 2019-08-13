package net.dumbcode.dumblibrary.server.ecs.system;

import net.dumbcode.dumblibrary.server.ecs.EntityManager;

public interface EntitySystem {
    void populateBuffers(EntityManager manager);

    void update();
}
