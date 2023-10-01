package net.dumbcode.dumblibrary.server.utils;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.IPlantable;

@AllArgsConstructor
public class PlantableDelegate implements IPlantable {

    private final BlockState state;


    @Override
    public BlockState getPlant(IBlockReader world, BlockPos pos) {
        return this.state;
    }
}
