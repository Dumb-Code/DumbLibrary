package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;

public enum MetabolismSystem implements EntitySystem {
    INSTANCE;
    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.METABOLISM);
        this.metabolism = family.populateBuffer(EntityComponentTypes.METABOLISM, this.metabolism);
        this.entities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.metabolism.length; i++) {
            if(this.entities[i].ticksExisted % 20 == 0) {
                MetabolismComponent meta = metabolism[i];
                meta.food -= meta.foodRate;
                meta.water -= meta.waterRate;

                // TODO: Hurt the entity when it has no more food / water left.
            }
        }
    }


}
