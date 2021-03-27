package net.dumbcode.dumblibrary.server.ecs.blocks;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.GroupedComponentAccess;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

import java.util.Arrays;
import java.util.Optional;

public interface BlockPropertyAccess {

    Property<? extends ComponentAccess>[] getComponentProperties();

    static Optional<ComponentAccess> getAccessFromState(BlockState state) {
        return state != null && state.getBlock() instanceof BlockPropertyAccess ?
                Optional.of(new GroupedComponentAccess(
                        Arrays.stream(((BlockPropertyAccess) state.getBlock())
                                .getComponentProperties())
                                .map(state::getValue)
                                .toArray(ComponentAccess[]::new)
                )) :
                Optional.empty();

    }
}
