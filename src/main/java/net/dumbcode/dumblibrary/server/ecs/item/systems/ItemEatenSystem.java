package net.dumbcode.dumblibrary.server.ecs.item.systems;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.item.ItemCompoundAccess;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.dumblibrary.server.events.UseItemEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemEatenSystem implements EntitySystem {

    @SubscribeEvent
    public void onItemDuration(UseItemEvent.Duration event) {
        ItemCompoundAccess.getAccess(event.getStack()).flatMap(EntityComponentTypes.ITEM_EATEN.get()).ifPresent(c -> event.setDuration(c.getDuration()));
    }

    @SubscribeEvent
    public void onItemAction(UseItemEvent.Action event) {
        ItemCompoundAccess.getAccess(event.getStack()).flatMap(EntityComponentTypes.ITEM_EATEN.get()).ifPresent(c -> event.setAction(UseAction.EAT));
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();

        ItemStack itemstack = player.getItemInHand(hand);

        ItemCompoundAccess.getAccess(itemstack).flatMap(EntityComponentTypes.ITEM_EATEN.get()).ifPresent(c -> {
            if (player.canEat(c.isIgnoreHunger())) {
                player.startUsingItem(hand);
                event.setCancellationResult(ActionResultType.SUCCESS);
            } else {
                event.setCancellationResult(ActionResultType.FAIL);
            }
            event.setCanceled(true);
        });
    }

    @SubscribeEvent
    public void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        ItemCompoundAccess.getAccess(event.getItem()).flatMap(a -> a.get(EntityComponentTypes.ITEM_EATEN)).ifPresent(c -> {
            LivingEntity living = event.getEntityLiving();
            if(living instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) living;
                player.getFoodData().eat(c.getFillAmount(), c.getSaturation());
                c.getPotionEffectList().forEach(player::addEffect);
            }
        });
        event.getResultStack().shrink(1);
    }
}
