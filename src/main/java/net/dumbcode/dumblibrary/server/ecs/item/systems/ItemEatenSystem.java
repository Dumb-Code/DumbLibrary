package net.dumbcode.dumblibrary.server.ecs.item.systems;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.item.ItemCompoundAccess;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.dumblibrary.server.events.UseItemEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum ItemEatenSystem implements EntitySystem {
    INSTANCE;

    @SubscribeEvent
    public void onItemDuration(UseItemEvent.Duration event) {
        ItemCompoundAccess.getAccess(event.getStack()).flatMap(EntityComponentTypes.ITEM_EATEN).ifPresent(c -> event.setDuration(c.getDuration()));
    }

    @SubscribeEvent
    public void onItemAction(UseItemEvent.Action event) {
        ItemCompoundAccess.getAccess(event.getStack()).flatMap(EntityComponentTypes.ITEM_EATEN).ifPresent(c -> event.setAction(EnumAction.EAT));
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();

        ItemStack itemstack = player.getHeldItem(hand);

        ItemCompoundAccess.getAccess(itemstack).flatMap(EntityComponentTypes.ITEM_EATEN).ifPresent(c -> {
            if (player.canEat(c.isIgnoreHunger())) {
                player.setActiveHand(hand);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            } else {
                event.setCancellationResult(EnumActionResult.FAIL);
            }
            event.setCanceled(true);
        });
    }

    @SubscribeEvent
    public void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        ItemCompoundAccess.getAccess(event.getItem()).flatMap(a -> a.get(EntityComponentTypes.ITEM_EATEN)).ifPresent(c -> {
            EntityLivingBase living = event.getEntityLiving();
            if(living instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) living;
                player.getFoodStats().addStats(c.getFillAmount(), c.getSaturation());
                c.getPotionEffectList().forEach(player::addPotionEffect);
            }
        });
        event.getResultStack().shrink(1);
    }
}
