package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import com.google.common.base.Optional;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.GrowingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class GrowingSystem implements EntitySystem {
    int updateLCG = new Random().nextInt();

    @Override
    public void update(World world) {
        int speed = world.getGameRules().getInt("randomTickSpeed");
        world.getPersistentChunkIterable(((WorldServer)world).getPlayerChunkMap().getChunkIterator()).forEachRemaining(chunk -> {
            int chunkX = chunk.x * 16;
            int chunkZ = chunk.z * 16;
            for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                if (storage != Chunk.NULL_BLOCK_STORAGE) {
                    for (int tick = 0; tick < speed; ++tick) {
                        //Match math on server world
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int update = this.updateLCG >> 2;
                        int xPos = update & 15;
                        int zPos = update >> 8 & 15;
                        int yPos = update >> 16 & 15;

                        IBlockState iblockstate = storage.get(xPos, yPos, zPos);
                        BlockPropertyAccess.getAccessFromState(iblockstate)
                                .flatMap(EntityComponentTypes.BLOCK_GROWING)
                                .ifPresent(component -> this.growComponent(component, component.getBlockProperty(), world, iblockstate, new BlockPos(xPos + chunkX, yPos + storage.getYLocation(), zPos + chunkZ)));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onBoneMealEvent(BonemealEvent event) {
        BlockPropertyAccess.getAccessFromState(event.getBlock())
                .flatMap(EntityComponentTypes.BLOCK_GROWING)
                .ifPresent(component -> {
                    this.growComponent(component, component.getBlockProperty(), event.getWorld(), event.getBlock(), event.getPos());
                    event.getWorld().playEvent(2005, event.getPos(), 0);
                });
    }

    private <T extends Comparable<T>> void growComponent(GrowingComponent component, IProperty<T> property, World world, IBlockState iblockstate, BlockPos pos) {
        Optional<T> value = property != null ? property.parseValue(component.getGrowTo(world.rand)) : Optional.absent();
        if(value.isPresent()) {
            IBlockState toState = iblockstate.withProperty(property, value.get());
            world.setBlockState(pos, toState);
        }
    }
}
