package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.block.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;

@Getter
@Setter
public class BlockPlaceableComponent extends EntityComponent {

    private PlaceablePredicate predicate = (world, pos, state) -> true;

    public interface PlaceablePredicate {
        boolean canPlace(World world, BlockPos pos, BlockState state);
    }

}
