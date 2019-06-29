package net.dumbcode.dumblibrary.server.entity.ai;

import net.dumbcode.dumblibrary.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.dumblibrary.server.utils.AIUtils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class DrinkingAI extends EntityAIBase {

    private EntityLiving entity;
    private BlockPos pos;
    private MetabolismComponent metabolism;

    private final int waterThreshold = 10;

    public DrinkingAI(EntityLiving entity, MetabolismComponent metabolism) {
        this.entity = entity;
        this.metabolism = metabolism;
    }

    @Override
    public boolean shouldExecute() {
        if(metabolism.water >= waterThreshold || true) {
            List<BlockPos> pos = AIUtils.traverseXZ((int) entity.posX, (int) entity.posY - 1, (int) entity.posZ, 10);
            for (BlockPos bPos : pos) {
                if (entity.world.getBlockState(bPos).getMaterial() == Material.WATER) {
                    this.pos = bPos;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        entity.getNavigator().tryMoveToXYZ(pos.getX() + 1, pos.getY(), pos.getZ(), entity.getAIMoveSpeed());
        metabolism.water += metabolism.waterRate * 10;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return metabolism.water >= waterThreshold;
    }
}