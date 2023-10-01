package net.dumbcode.dumblibrary.server;

import net.dumbcode.dumblibrary.server.events.UseItemEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

public class ItemComponent extends Item {

    public ItemComponent(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        UseItemEvent.Duration event = new UseItemEvent.Duration(stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getDuration();
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        UseItemEvent.Action event = new UseItemEvent.Action(stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAction();
    }

    @Override
    public void onUseTick(World world, LivingEntity entity, ItemStack stack, int count) {
        if (!stack.isEmpty() && entity.isUsingItem() && count <= 25 && count % 4 == 0) {
            this.updateEatingParticles(stack, entity, 5);
        }
        super.onUseTick(world, entity, stack, count);
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entityLiving) {
        if (!stack.isEmpty() && entityLiving.isUsingItem()) {
            this.updateEatingParticles(stack, entityLiving, 16);
        }
        return super.finishUsingItem(stack, world, entityLiving);
    }

    private void updateEatingParticles(ItemStack stack, LivingEntity entity, int particleCount) {
        //Copied vanilla code from LivingEntity#spawnItemParticles
        if(!entity.level.isClientSide && stack.getUseAnimation() == UseAction.EAT) {
            for (int i = 0; i < particleCount; ++i) {
                Vector3d vector3d = new Vector3d(((double) random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                vector3d = vector3d.xRot(-entity.xRot * ((float) Math.PI / 180F));
                vector3d = vector3d.yRot(-entity.yRot * ((float) Math.PI / 180F));
                double d0 = (double) (-random.nextFloat()) * 0.6D - 0.3D;
                Vector3d vector3d1 = new Vector3d(((double) random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
                vector3d1 = vector3d1.xRot(-entity.xRot * ((float) Math.PI / 180F));
                vector3d1 = vector3d1.yRot(-entity.yRot * ((float) Math.PI / 180F));
                vector3d1 = vector3d1.add(entity.getX(), entity.getEyeY(), entity.getZ());
                ((ServerWorld) entity.level).sendParticles(new ItemParticleData(ParticleTypes.ITEM, stack), vector3d1.x, vector3d1.y, vector3d1.z, 1, vector3d.x, vector3d.y + 0.05D, vector3d.z, 0.0D);
            }
        }
    }
}
