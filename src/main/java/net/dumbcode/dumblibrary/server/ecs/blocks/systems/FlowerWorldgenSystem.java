package net.dumbcode.dumblibrary.server.ecs.blocks.systems;

import com.google.common.collect.Iterables;
import net.dumbcode.dumblibrary.server.ecs.BlockstateManager;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.FlowerWorldgenComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.dumblibrary.server.utils.PlantableDelegate;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FlowerWorldgenSystem implements EntitySystem {

    private IBlockState[] blockstates = new IBlockState[0];
    private FlowerWorldgenComponent[] flowerGenComponents = new FlowerWorldgenComponent[0];

    public FlowerWorldgenSystem() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
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

                List<IProperty<?>> randomProperties = state.getPropertyKeys()
                        .stream()
                        .filter(iProperty -> component.getRandomizedProperties().contains(iProperty.getName()))
                        .collect(Collectors.toList());

                if(component.getBiomeTypes().contains(baseBiome) && random.nextFloat() < component.getChancePerChunk()) {
                    for (int groupid = 0; groupid < component.getGroupSpawnSize() + random.nextGaussian() * component.getGroupSpawnSize()/2D; groupid++) {
                        BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(startX + random.nextInt(24), 100, startZ + random.nextInt(24)));
                        IBlockState soil = world.getBlockState(pos.down());

                        IBlockState placeState = state;
                        for (IProperty<?> property : randomProperties) {
                            placeState = randomizeProperty(placeState, property, world.rand);
                        }

                        if(world.isAirBlock(pos) && soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, new PlantableDelegate(component.getPlantType(), placeState))) {
                            world.setBlockState(pos, placeState);
                        }
                    }
                }
            }
        }
    }

    private static <T extends Comparable<T>> IBlockState randomizeProperty(IBlockState state, IProperty<T> property, Random rand) {
        return state.withProperty(property, Iterables.get(property.getAllowedValues(), rand.nextInt(property.getAllowedValues().size())));
    }

}
