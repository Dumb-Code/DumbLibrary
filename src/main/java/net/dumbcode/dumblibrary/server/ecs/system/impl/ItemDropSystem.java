package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ItemDropComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//TODO: remove this, it's not really needed
public class ItemDropSystem implements EntitySystem {

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        //NO OP
    }

    @Override
    public void update(World world) {
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
