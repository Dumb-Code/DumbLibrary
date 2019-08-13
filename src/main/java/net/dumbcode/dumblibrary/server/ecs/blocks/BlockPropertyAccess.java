package net.dumbcode.dumblibrary.server.ecs.blocks;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Optional;

public interface BlockPropertyAccess {

    BlockstateComponentProperty getComponentProperty();

    static Optional<BlockstateComponentProperty> getAccessFromState(IBlockState state) {
        return state.getBlock() instanceof BlockPropertyAccess ? Optional.of(((BlockPropertyAccess) state.getBlock()).getComponentProperty()) : Optional.empty();
    }
}
