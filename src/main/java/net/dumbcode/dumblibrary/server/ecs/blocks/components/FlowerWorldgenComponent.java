package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.PlantType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FlowerWorldgenComponent extends EntityComponent {

    private final List<RegistryKey<Biome>> biomeTypes = new ArrayList<>();
    private final List<String> randomizedProperties = new ArrayList<>();
    private PlantType plantType;
    private float chancePerChunk = 0F;
    private int groupSpawnSize = 5;

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<FlowerWorldgenComponent> {

        private List<String> biomeTypes = new ArrayList<>();
        private List<String> randomizedProperties = new ArrayList<>();
        private PlantType plantType;
        private float chancePerStatePerChunk = 0F;
        private int groupSpawnSize = 5;

        @Override
        public void constructTo(FlowerWorldgenComponent component) {
            for (String biomeType : this.biomeTypes) {
                component.biomeTypes.addAll(BiomeDictionary.getBiomes(BiomeDictionary.Type.getType(biomeType)));
            }

            component.randomizedProperties.addAll(this.randomizedProperties);
            component.plantType = this.plantType;
            component.chancePerChunk = this.chancePerStatePerChunk;
            component.groupSpawnSize = this.groupSpawnSize;
        }

        @Override
        public void readJson(JsonObject json) {
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "spawnable_biomes"))
                    .map(elem -> JSONUtils.convertToString(elem, "element"))
                    .forEach(this.biomeTypes::add);
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "randomized_properties"))
                    .map(elem -> JSONUtils.convertToString(elem, "element"))
                    .forEach(this.randomizedProperties::add);
            this.plantType = PlantType.get(JSONUtils.getAsString(json, "plant_type"));
            this.chancePerStatePerChunk = JSONUtils.getAsFloat(json, "chance_per_chunk");
            this.groupSpawnSize = JSONUtils.getAsInt(json, "group_spawn_size");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("spawnable_biomes", this.biomeTypes.stream().collect(CollectorUtils.toJsonArrayString()));
            json.add("randomized_properties", this.randomizedProperties.stream().collect(CollectorUtils.toJsonArrayString()));
            json.addProperty("plant_type", this.plantType.getName());
            json.addProperty("chance_per_chunk", this.chancePerStatePerChunk);
            json.addProperty("group_spawn_size", this.groupSpawnSize);
        }
    }

}
