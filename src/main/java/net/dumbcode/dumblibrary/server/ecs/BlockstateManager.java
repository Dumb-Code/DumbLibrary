package net.dumbcode.dumblibrary.server.ecs;

import net.dumbcode.dumblibrary.server.ecs.blocks.BlockPropertyAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public enum BlockstateManager {
    INSTANCE;

    private boolean setup = false;
    private IBlockState[] applicibleStates = new IBlockState[0];

    private void setup() {
        this.setup = true;
        List<IBlockState> accessList = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS) {
            if(block instanceof BlockPropertyAccess) {
                accessList.addAll(block.getBlockState().getValidStates());
            }
        }
        this.applicibleStates = accessList.toArray(new IBlockState[0]);
    }

    public EntityFamily<IBlockState> resolveFamily(EntityComponentType<?, ?>... types) {
        if(!this.setup) {
            this.setup();
        }
        List<IBlockState> blockstates = new ArrayList<>(this.applicibleStates.length);
        for (IBlockState state : this.applicibleStates) {
            ComponentAccess stateValue = BlockPropertyAccess.getAccessFromState(state).orElseThrow(NullPointerException::new);
            if (stateValue.matchesAll(types)) {
                blockstates.add(state);
            }
        }
        return new EntityFamily<>(blockstates.toArray(new IBlockState[0]), state -> BlockPropertyAccess.getAccessFromState(state).orElseThrow(NullPointerException::new));
    }

}
