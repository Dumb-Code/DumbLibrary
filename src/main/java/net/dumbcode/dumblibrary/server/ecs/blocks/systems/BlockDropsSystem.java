package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum BlockDropsSystem implements EntitySystem {
    INSTANCE;

    @SubscribeEvent
    public void onBlockDrop(BlockEvent.HarvestDropsEvent event) {
        BlockPropertyAccess.getAccessFromState(event.getState()).flatMap(EntityComponentTypes.BLOCK_DROPS).ifPresent(drops -> drops.applyStacks(event.getDrops()::add));
    }
}
