package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public enum BlockstateManager {
    INSTANCE;

    private boolean setup = false;
    private BlockState[] applicibleStates = new BlockState[0];

    private void setup() {
        this.setup = true;
        List<BlockState> accessList = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS) {
            if(block instanceof BlockPropertyAccess) {
                accessList.addAll(block.getStateDefinition().getPossibleStates());
            }
        }
        this.applicibleStates = accessList.toArray(new BlockState[0]);
    }

    public EntityFamily<BlockState> resolveFamily(EntityComponentType<?, ?>... types) {
        if(!this.setup) {
            this.setup();
        }
        List<BlockState> blockstates = new ArrayList<>(this.applicibleStates.length);
        for (BlockState state : this.applicibleStates) {
            ComponentAccess stateValue = BlockPropertyAccess.getAccessFromState(state).orElseThrow(NullPointerException::new);
            if (stateValue.matchesAll(types)) {
                blockstates.add(state);
            }
        }
        return new EntityFamily<>(blockstates.toArray(new BlockState[0]), state -> BlockPropertyAccess.getAccessFromState(state).orElseThrow(NullPointerException::new));
    }

    @SafeVarargs
    public final EntityFamily<BlockState> resolveFamily(Supplier<? extends EntityComponentType<?, ?>>... types) {
        return this.resolveFamily(Arrays.stream(types).map(Supplier::get).toArray(EntityComponentType<?, ?>[]::new));
    }
}
