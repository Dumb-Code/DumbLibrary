package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.EnumPlantType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Getter
public class FlowerWorldgenComponent implements EntityComponent {

    private List<Biome> biomeTypes = new ArrayList<>();
    private EnumPlantType plantType;
    private float chancePerChunk = 0F;
    private int groupSpawnSize = 5;

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<FlowerWorldgenComponent> {

        private List<String> biomeTypes = new ArrayList<>();
        private EnumPlantType plantType;
        private float chancePerChunk = 0F;
        private int groupSpawnSize = 5;

        @Override
        public FlowerWorldgenComponent construct() {
            FlowerWorldgenComponent component = new FlowerWorldgenComponent();

            for (String biomeType : this.biomeTypes) {
                component.biomeTypes.addAll(BiomeDictionary.getBiomes(BiomeDictionary.Type.getType(biomeType)));
            }

            component.plantType = this.plantType;
            component.chancePerChunk = this.chancePerChunk;
            component.groupSpawnSize = this.groupSpawnSize;

            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            StreamSupport.stream(JsonUtils.getJsonArray(json, "spawnable_biomes").spliterator(), false)
                    .map(elem -> JsonUtils.getString(elem, "element"))
                    .forEach(this.biomeTypes::add);
            this.plantType = EnumPlantType.getPlantType(JsonUtils.getString(json, "plant_type"));
            this.chancePerChunk = JsonUtils.getFloat(json, "chance_per_chunk");
            this.groupSpawnSize = JsonUtils.getInt(json, "group_spawn_size");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("spawnable_biomes", this.biomeTypes.stream().collect(IOCollectors.toJsonArrayString()));
            json.addProperty("plant_type", this.plantType.name());
            json.addProperty("chance_per_chunk", this.chancePerChunk);
            json.addProperty("group_spawn_size", this.groupSpawnSize);
        }
    }

}
