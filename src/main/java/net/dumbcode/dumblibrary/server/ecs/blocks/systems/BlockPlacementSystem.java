package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockPlacementSystem implements EntitySystem {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof ItemBlock && event.getFace() != null) {
            ItemBlock item = (ItemBlock) stack.getItem();
            IBlockState placement = item.getBlock().getStateForPlacement(event.getWorld(), event.getPos().offset(event.getFace()), event.getFace(), (float) event.getHitVec().x, (float) event.getHitVec().y, (float) event.getHitVec().z, stack.getMetadata(), event.getEntityLiving(), event.getHand());
            BlockPropertyAccess.getAccessFromState(placement)
                    .flatMap(EntityComponentTypes.BLOCK_PLACEABLE)
                    .filter(c -> !c.getPredicate().canPlace(event.getWorld(), event.getPos().offset(event.getFace()), placement))
                    .ifPresent(c -> {
                        event.setCancellationResult(EnumActionResult.FAIL);
                        event.setCanceled(true);
                    });
        }


    }
}
