package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public class EntityManagerListener {

    @SubscribeEvent
    public static void onEntityAdded(EntityJoinWorldEvent event) {
        event.getWorld().getCapability(DumbLibrary.ENTITY_MANAGER).ifPresent(entityManager -> entityManager.addEntity(event.getEntity()));
    }

    @SubscribeEvent
    public static void onEntityRemoved(EntityLeaveWorldEvent event) {
        event.getWorld().getCapability(DumbLibrary.ENTITY_MANAGER).ifPresent(entityManager -> entityManager.removeEntity(event.getEntity()));
    }
}
