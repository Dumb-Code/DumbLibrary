package net.dumbcode.dumblibrary.server.ecs.system;

import net.dumbcode.dumblibrary.server.ecs.BlockstateManager;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.minecraft.world.World;

public interface EntitySystem {
    default void populateEntityBuffers(EntityManager manager) {
        //NO-OP
    }

    default void populateBlockstateBuffers(BlockstateManager manager) {
        //NO-OP
    }

    default void update(World world) {
        //NO-OP
    }
}
