package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.GrowingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class GrowingSystem implements EntitySystem {
    int updateLCG = new Random().nextInt();

    @Override
    public void update(World world) {
        if(world instanceof ServerWorld) {
            ChunkManager chunkMap = ((ServerChunkProvider) world.getChunkSource()).chunkMap;
            List<ChunkHolder> chunks = new ArrayList<>(chunkMap.visibleChunkMap.values());
            Collections.shuffle(chunks);
            for (ChunkHolder holder : chunks) {
                Chunk chunk = holder.getTickingChunk();
                for (ChunkSection section : chunk.getSections()) {
                    if(section != Chunk.EMPTY_SECTION) {
                        int x = chunk.getPos().getMinBlockX();
                        int y = section.bottomBlockY();
                        int z = chunk.getPos().getMinBlockZ();
                        BlockPos randomPos = world.getBlockRandomPos(x, y, z, 15);
                        BlockState state = section.getBlockState(randomPos.getX() - x, randomPos.getY() - y, randomPos.getZ() - z);

                        BlockPropertyAccess.getAccessFromState(state)
                            .flatMap(EntityComponentTypes.BLOCK_GROWING)
                            .ifPresent(component -> this.growComponent(component, component.getBlockProperty(), world, state, randomPos));

                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBoneMealEvent(BonemealEvent event) {
        BlockPropertyAccess.getAccessFromState(event.getBlock())
                .flatMap(EntityComponentTypes.BLOCK_GROWING)
                .ifPresent(component -> {
                    this.growComponent(component, component.getBlockProperty(), event.getWorld(), event.getBlock(), event.getPos());
                    event.getWorld().blockEvent(event.getPos(), event.getBlock().getBlock(), 2005 , 0);
                });
    }

    private <T extends Comparable<T>> void growComponent(GrowingComponent component, Property<T> property, World world, BlockState iblockstate, BlockPos pos) {
        Optional<T> value = property != null ? property.getValue(component.getGrowTo(world.random)) : Optional.empty();
        if(value.isPresent()) {
            BlockState toState = iblockstate.setValue(property, value.get());
            world.setBlock(pos, toState, 3);
        }
    }
}
