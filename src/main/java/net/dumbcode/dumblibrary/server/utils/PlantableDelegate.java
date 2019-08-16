package net.dumbcode.dumblibrary.server.utils;

import lombok.AllArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

@AllArgsConstructor
public class PlantableDelegate implements IPlantable {

    private final EnumPlantType plantType;
    private final IBlockState state;

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return this.plantType;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return this.state;
    }
}
