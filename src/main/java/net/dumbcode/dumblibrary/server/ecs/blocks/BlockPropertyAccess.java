package net.dumbcode.dumblibrary.server.ecs.blocks;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraft.block.state.IBlockState;

import java.util.Optional;

public interface BlockPropertyAccess {

    BlockstateComponentProperty getComponentProperty();

    static Optional<BlockstateComponentProperty> getProperty(IBlockState state) {
        return state.getBlock() instanceof BlockPropertyAccess ? Optional.of(((BlockPropertyAccess) state.getBlock()).getComponentProperty()) : Optional.empty();
    }

    static Optional<ComponentAccess> getAccessFromState(IBlockState state) {
        return getProperty(state).map(state::getValue);
    }
}
