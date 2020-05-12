package net.dumbcode.dumblibrary.server.ecs.blocks.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockPlaceableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.PlantableDelegate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.EnumPlantType;

@Accessors(chain = true)
@Getter
@Setter
public class FlowerBlockPlaceableStorage implements EntityComponentStorage<BlockPlaceableComponent> {

    private EnumPlantType plantType;

    @Override
    public void constructTo(BlockPlaceableComponent component) {
        component.setPredicate((world, pos, state) -> {//worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)
            IBlockState soil = world.getBlockState(pos.down());
            return world.getBlockState(pos).getBlock().isReplaceable(world, pos) && soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, new PlantableDelegate(this.plantType, state));
        });
    }

    @Override
    public void readJson(JsonObject json) {
        this.plantType = EnumPlantType.getPlantType(JsonUtils.getString(json, "plant_type"));
    }

    @Override
    public void writeJson(JsonObject json) {
        json.addProperty("plant_type", this.plantType.name());
    }
}
