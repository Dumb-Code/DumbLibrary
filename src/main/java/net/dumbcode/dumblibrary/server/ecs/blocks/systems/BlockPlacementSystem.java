package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockPlacementSystem implements EntitySystem {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof BlockItem && event.getFace() != null) {
            BlockItem item = (BlockItem) stack.getItem();
            BlockItemUseContext context = new BlockItemUseContext(
                event.getPlayer(), event.getHand(), event.getItemStack(), event.getHitVec()
            );
            BlockState placement = item.getBlock().getStateForPlacement(context);
            BlockPropertyAccess.getAccessFromState(placement)
                    .flatMap(EntityComponentTypes.BLOCK_PLACEABLE.get())
                    .filter(c -> !c.getPredicate().canPlace(event.getWorld(), event.getPos().relative(event.getFace()), placement))
                    .ifPresent(c -> {
                        event.setCancellationResult(ActionResultType.FAIL);
                        event.setCanceled(true);
                    });
        }


    }
}
