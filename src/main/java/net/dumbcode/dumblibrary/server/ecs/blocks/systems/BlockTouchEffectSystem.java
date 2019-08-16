package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.BlockstateManager;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockTouchEffectComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public enum BlockTouchEffectSystem implements EntitySystem {
    INSTANCE;

    private final List<IBlockState> states = new LinkedList<>();
    private BlockTouchEffectComponent[] components = new BlockTouchEffectComponent[0];

    @Override
    public void populateBlockstateBuffers(BlockstateManager manager) {
        this.states.clear();
        EntityFamily<IBlockState> family = manager.resolveFamily(EntityComponentTypes.BLOCK_TOUCH_EFFECT);

        Collections.addAll(this.states, family.getEntities());
        this.components = family.populateBuffer(EntityComponentTypes.BLOCK_TOUCH_EFFECT, this.components);
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        AxisAlignedBB bb = entity.getEntityBoundingBox().grow(0.1D);
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(bb.minX, bb.minY, bb.minZ), new BlockPos(bb.maxX, bb.maxY, bb.maxZ))) {
            IBlockState blockState = entity.world.getBlockState(pos);

            int index = this.states.indexOf(blockState);
            if(index != -1) {
                BlockTouchEffectComponent component = this.components[index];
                for (PotionEffect effect : component.getPotionEffectList()) {
                    if(entity.isPotionApplicable(effect)) {
                        if(!entity.isPotionActive(effect.getPotion()) || entity.ticksExisted % 80 == 0) {
                            entity.addPotionEffect(effect);
                        }
                    }
                }
            }
        }
    }
}
