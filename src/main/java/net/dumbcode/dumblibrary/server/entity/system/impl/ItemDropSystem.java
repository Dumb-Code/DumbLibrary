package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.additionals.ItemDropComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum ItemDropSystem implements EntitySystem {
    INSTANCE;

    @Override
    public void populateBuffers(EntityManager manager) {
        //NO OP
    }

    @Override
    public void update() {
        //NO OP
    }

    @SubscribeEvent
    public void onDrops(LivingDropsEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess) {
            ComponentAccess access = (ComponentAccess) entity;

            for (EntityComponent component : access.getAllComponents()) {
                if(component instanceof ItemDropComponent) {
                    ((ItemDropComponent) component).collectItems(access, stack -> entity.entityDropItem(stack, 0F));
                }
            }

        }
    }
}
