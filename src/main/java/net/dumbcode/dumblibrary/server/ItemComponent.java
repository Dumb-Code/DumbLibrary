package net.dumbcode.dumblibrary.server;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.events.UseItemEvent;
import net.dumbcode.dumblibrary.server.network.S1PlayItemCrackParticle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ItemComponent extends Item {

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        UseItemEvent.Duration event = new UseItemEvent.Duration(stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDuration();
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        UseItemEvent.Action event = new UseItemEvent.Action(stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAction();
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        if (!stack.isEmpty() && player.isHandActive() && count <= 25 && count % 4 == 0) {
            this.updateEatingParticles(stack, player, 5);
        }
        super.onUsingTick(stack, player, count);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!stack.isEmpty() && entityLiving.isHandActive()) {
            this.updateEatingParticles(stack, entityLiving, 16);
        }
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    private void updateEatingParticles(ItemStack stack, EntityLivingBase entity, int particleCount) {
        //Copied vanilla code
        if(!entity.world.isRemote && stack.getItemUseAction() == EnumAction.EAT) {
            for (int i = 0; i < particleCount; ++i) {

                Vec3d vec3d = new Vec3d(((double)entity.getRNG().nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vec3d = vec3d.rotatePitch(-entity.rotationPitch * 0.017453292F);
                vec3d = vec3d.rotateYaw(-entity.rotationYaw * 0.017453292F);
                Vec3d speed = new Vec3d(vec3d.x, vec3d.y, vec3d.z);

                double d0 = (double)(-entity.getRNG().nextFloat()) * 0.6D - 0.3D;
                Vec3d vec3d1 = new Vec3d(((double)entity.getRNG().nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
                vec3d1 = vec3d1.rotatePitch(-entity.rotationPitch * 0.017453292F);
                vec3d1 = vec3d1.rotateYaw(-entity.rotationYaw * 0.017453292F);
                vec3d1 = vec3d1.add(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
                Vec3d pos = new Vec3d(vec3d1.x, vec3d1.y + 0.05D, vec3d1.z);

                DumbLibrary.NETWORK.sendToDimension(new S1PlayItemCrackParticle(pos, speed, stack), entity.world.provider.getDimension());
            }
        }
    }
}
