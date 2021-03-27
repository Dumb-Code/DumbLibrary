package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class WorldUtils {
    public static Optional<Entity> getEntityFromUUID(World world, UUID uuid) {
        if(world instanceof ClientWorld) {
            for (Entity entity : ((ClientWorld) world).entitiesForRendering()) {
                if(entity.getUUID().equals(uuid)) {
                    return Optional.of(entity);
                }
            }
        } else if(world instanceof ServerWorld) {
            return Optional.ofNullable(((ServerWorld) world).getEntity(uuid));
        }
        DumbLibrary.getLogger().warn("Don't know how to handle a world of type " + world.getClass());
        return Optional.empty();
    }

    public static BlockPos getTopNonLeavesBlock(World world, BlockPos pos, Predicate<BlockState> statePredicate) {
        IChunk chunk = world.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getHighestSectionPosition() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.below();
            BlockState state = chunk.getBlockState(blockpos1);

            if (statePredicate.test(state) && !state.getBlock().is(BlockTags.LEAVES))
            {
                break;
            }
        }

        return blockpos;
    }

    public static BlockPos getDirectTopdownBlock(World world, BlockPos pos) {

        IChunk chunk = world.getChunk(pos);
        BlockPos blockpos;
        BlockPos blockpos1;

        for (blockpos = new BlockPos(pos.getX(), chunk.getHighestSectionPosition() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
            blockpos1 = blockpos.below();
            BlockState state = chunk.getBlockState(blockpos1);

            if ((state.getMaterial().isLiquid() || (state.getMaterial().blocksMotion() && !state.getMaterial().isReplaceable()))
                && !state.getBlock().is(BlockTags.LEAVES)) {
                break;
            }
        }

        return blockpos;
    }

}
