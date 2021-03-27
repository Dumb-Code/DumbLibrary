package net.dumbcode.dumblibrary.server.utils;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

@AllArgsConstructor
public class PlantableDelegate implements IPlantable {

    private final BlockState state;


    @Override
    public BlockState getPlant(IBlockReader world, BlockPos pos) {
        return this.state;
    }
}
