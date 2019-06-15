package net.dumbcode.dumblibrary.server.entity.system;

import net.dumbcode.dumblibrary.server.entity.EntityManager;

public interface EntitySystem {
    void populateBuffers(EntityManager manager);

    void update();
}
