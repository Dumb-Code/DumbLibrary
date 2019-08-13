package net.dumbcode.dumblibrary.server.ecs.system;

import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.minecraft.world.World;

public interface EntitySystem {
    default void populateBuffers(EntityManager manager) {
        //NO-OP
    }

    void update(World world);
}
