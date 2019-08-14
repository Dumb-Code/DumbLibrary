package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import net.dumbcode.dumblibrary.server.ecs.BlockstateManager;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.FlowerWorldgenComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public enum FlowerWorldgenSystem implements EntitySystem {
    INSTANCE;


    private IBlockState[] blockstates = new IBlockState[0];
    private FlowerWorldgenComponent[] flowerGenComponents = new FlowerWorldgenComponent[0];

    FlowerWorldgenSystem() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }


    @Override
    public void update(World world) {
        //NO-OP
    }

    @Override
    public void populateBlockstateBuffers(BlockstateManager manager) {
        EntityFamily<IBlockState> family = manager.resolveFamily(EntityComponentTypes.FLOWER_WORLDGEN);
        this.blockstates = family.getEntities();
        this.flowerGenComponents = family.populateBuffer(EntityComponentTypes.FLOWER_WORLDGEN, this.flowerGenComponents);
    }

    @SubscribeEvent
    public void onChunkDecorate(DecorateBiomeEvent.Decorate event) {
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.FLOWERS) {
            Random random = event.getRand();
            World world = event.getWorld();
            ChunkPos chunkPos = event.getChunkPos();

            int startX = chunkPos.getXStart() + 4;
            int startZ = chunkPos.getZStart() + 4;

            Biome baseBiome = world.getBiome(new BlockPos(startX, 0, startZ));

            for (int i = 0; i < this.blockstates.length; i++) {
                IBlockState state = this.blockstates[i];
                FlowerWorldgenComponent component = this.flowerGenComponents[i];

                if(component.getBiomeTypes().contains(baseBiome) && random.nextFloat() < component.getChancePerChunk() + random.nextGaussian() * component.getChancePerChunk()/4D) {
                    for (int groupid = 0; groupid < component.getGroupSpawnSize(); groupid++) {
                        BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(startX + random.nextInt(24), 100, startZ + random.nextInt(24)));
                        IBlockState soil = world.getBlockState(pos.down());
                        if(world.isAirBlock(pos) && soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, new DummyPlantable(state, component.getPlantType()))) {
                            world.setBlockState(pos, state);
                        }
                    }
                }
            }

        }
    }

    private static class DummyPlantable implements IPlantable {

        private final IBlockState state;
        private final EnumPlantType plantType;

        private DummyPlantable(IBlockState state, EnumPlantType plantType) {
            this.state = state;
            this.plantType = plantType;
        }

        @Override
        public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
            return this.plantType;
        }

        @Override
        public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
            return this.state;
        }
    }

}
