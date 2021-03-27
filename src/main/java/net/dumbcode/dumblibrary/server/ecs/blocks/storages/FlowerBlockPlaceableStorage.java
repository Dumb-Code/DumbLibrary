package net.dumbcode.dumblibrary.server.ecs.blocks.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.blocks.components.BlockPlaceableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.PlantType;

@Accessors(chain = true)
@Getter
@Setter
public class FlowerBlockPlaceableStorage implements EntityComponentStorage<BlockPlaceableComponent> {

    private PlantType plantType;

    @Override
    public void constructTo(BlockPlaceableComponent component) {
        component.setPredicate((world, pos, state) -> {//worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)
            BlockState soil = world.getBlockState(pos.below());
            return world.getBlockState(pos).getMaterial().isReplaceable() && soil.getBlock().canSustainPlant(soil, world, pos.below(), Direction.UP, (world1, pos1) -> state);
        });
    }

    @Override
    public void readJson(JsonObject json) {
        this.plantType = PlantType.get(JSONUtils.getAsString(json, "plant_type"));
    }

    @Override
    public void writeJson(JsonObject json) {
        json.addProperty("plant_type", this.plantType.getName());
    }
}
