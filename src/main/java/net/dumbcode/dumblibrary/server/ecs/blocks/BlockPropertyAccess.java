package net.dumbcode.dumblibrary.server.ecs.blocks;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.GroupedComponentAccess;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.Optional;

public interface BlockPropertyAccess {

    IProperty<? extends ComponentAccess>[] getComponentProperties();

    static Optional<ComponentAccess> getAccessFromState(IBlockState state) {
        return state.getBlock() instanceof BlockPropertyAccess ?
                Optional.of(new GroupedComponentAccess(
                        Arrays.stream(((BlockPropertyAccess) state.getBlock())
                                .getComponentProperties())
                                .map(state::getValue)
                                .toArray(ComponentAccess[]::new)
                )) :
                Optional.empty();

    }
}
