package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.minecraft.util.JsonUtils;

import java.util.Arrays;
import java.util.stream.StreamSupport;

public class GrowingComponent implements EntityComponent {

    @Getter private String[] growTo;

    @Getter
    public static class Storage implements EntityComponentStorage<GrowingComponent> {

        private String[] growTo;

        @Override
        public GrowingComponent construct() {
            GrowingComponent component = new GrowingComponent();
            component.growTo = this.growTo;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.growTo = StreamSupport.stream(JsonUtils.getJsonArray(json, "grow_to").spliterator(), false)
                    .filter(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isString())
                    .map(e -> e.getAsJsonPrimitive().getAsString())
                    .toArray(String[]::new);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("grow_to", Arrays.stream(this.growTo).map(JsonPrimitive::new).collect(IOCollectors.toJsonArray()));
        }

        public Storage setGrowTo(String... growTo) {
            this.growTo = growTo;
            return this;
        }
    }
}
