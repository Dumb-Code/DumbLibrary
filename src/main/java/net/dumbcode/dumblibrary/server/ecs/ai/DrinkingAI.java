package net.dumbcode.dumblibrary.server.ecs.ai;

import net.dumbcode.dumblibrary.server.ecs.component.impl.MetabolismComponent;
import net.dumbcode.dumblibrary.server.utils.AIUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class DrinkingAI extends EntityAIBase {

    private EntityLiving entity;
    private BlockPos pos;
    private MetabolismComponent metabolism;

    private static final int WATER_THRESHOLD = 10; // TODO: Vary

    public DrinkingAI(EntityLiving entity, MetabolismComponent metabolism) {
        this.entity = entity;
        this.metabolism = metabolism;
    }

    @Override
    public boolean shouldExecute() {
        if(metabolism.water >= WATER_THRESHOLD || true) {
            List<BlockPos> pos = AIUtils.traverseXZ((int) entity.posX, (int) entity.posY - 1, (int) entity.posZ, 10);
            for (BlockPos bPos : pos) {
                if (entity.world.getBlockState(bPos).getMaterial() == Material.WATER) {
                    this.pos = bPos;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        entity.getNavigator().tryMoveToXYZ((double) (pos.getX() + 1), pos.getY(), pos.getZ(), entity.getAIMoveSpeed());
        metabolism.water += metabolism.waterRate * 10;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return metabolism.water >= WATER_THRESHOLD;
    }
}