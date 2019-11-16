package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class WorldUtils {
    public static Optional<Entity> getEntityFromUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if(entity.getUniqueID().equals(uuid)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    public static BlockPos getTopNonLeavesBlock(World world, BlockPos pos, Predicate<IBlockState> statePredicate)
    {
        Chunk chunk = world.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1)
        {
            blockpos1 = blockpos.down();
            IBlockState state = chunk.getBlockState(blockpos1);

            if (statePredicate.test(state) && !state.getBlock().isLeaves(state, world, blockpos1) && !state.getBlock().isFoliage(world, blockpos1))
            {
                break;
            }
        }

        return blockpos;
    }

    public static BlockPos getDirectTopdownBlock(World world, BlockPos pos) {

        Chunk chunk = world.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.down();
            IBlockState state = chunk.getBlockState(blockpos1);

            if ((state.getMaterial().isLiquid() ||
                (state.getMaterial().blocksMovement() && !state.getBlock().isReplaceable(world, blockpos1))
            )
                &&
                !state.getBlock().isLeaves(state, world, blockpos1) &&
                !state.getBlock().isFoliage(world, blockpos1)
            ) {
                break;
            }
        }

        return blockpos;
    }

}
