package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Getter
@Setter
public class BlockPlaceableComponent implements EntityComponent {

    private PlaceablePredicate predicate = (world, pos, state) -> true;

    public interface PlaceablePredicate {
        boolean canPlace(World world, BlockPos pos, IBlockState state);
    }

}
