package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public enum FamilySystem implements EntitySystem {
    INSTANCE;

    private Entity[] breedingEntities;

    @Override
    public void populateEntityBuffers(EntityManager manager) {

    }

    @Override
    public void update(World world) {

    }
}
