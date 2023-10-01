package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.BlockstateManager;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockTouchEffectComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BlockTouchEffectSystem implements EntitySystem {

    private final List<BlockState> states = new LinkedList<>();
    private BlockTouchEffectComponent[] components = new BlockTouchEffectComponent[0];

    @Override
    public void populateBlockstateBuffers(BlockstateManager manager) {
        this.states.clear();
        EntityFamily<BlockState> family = manager.resolveFamily(EntityComponentTypes.BLOCK_TOUCH_EFFECT);

        Collections.addAll(this.states, family.getEntities());
        this.components = family.populateBuffer(EntityComponentTypes.BLOCK_TOUCH_EFFECT, this.components);
    }

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        AxisAlignedBB bb = entity.getBoundingBox().inflate(0.1D);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(bb.minX, bb.minY, bb.minZ), new BlockPos(bb.maxX, bb.maxY, bb.maxZ))) {
            BlockState blockState = entity.level.getBlockState(pos);

            int index = this.states.indexOf(blockState);
            if(index != -1) {
                BlockTouchEffectComponent component = this.components[index];
                for (EffectInstance effect : component.getPotionEffectList()) {
                    if(entity.canBeAffected(effect)) {
                        if(!entity.hasEffect(effect.getEffect()) || entity.tickCount % 80 == 0) {
                            entity.addEffect(effect);
                        }
                    }
                }
            }
        }
    }
}
