package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = DumbLibrary.MODID)
public enum GrowingSystem implements EntitySystem {
    INSTANCE;

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

                        //If the specified blockstate is a component access block, then get the access property.
                        //Then, with the component acess, get the #BLOCK_GROWING component type. If that is present,
                        //Then set a new block to the blocks position with the #getGrowTo as the property.
                        BlockPropertyAccess.getAccessFromState(iblockstate).ifPresent(iProperty -> {
                            iblockstate.getValue(iProperty).get(EntityComponentTypes.BLOCK_GROWING).ifPresent(component -> {
                                BlockPos pos = new BlockPos(xPos + chunkX, yPos + storage.getYLocation(), zPos + chunkZ);
                                IBlockState toState = iblockstate.withProperty(iProperty, iProperty.getFromString(component.getGrowTo()));
                                world.setBlockState(pos, toState);
                            });
                        });
                    }
                }
            }
        });
    }
}
