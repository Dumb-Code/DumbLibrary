package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.blocks.BlockstateComponentProperty;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.state.Property;
import net.minecraft.util.JSONUtils;

import java.util.Arrays;
import java.util.Random;

public class GrowingComponent extends EntityComponent implements FinalizableComponent {

    private String[] growTo;
    @Getter private Property<?> blockProperty;

    public String getGrowTo(Random rand) {
        return this.growTo[rand.nextInt(this.growTo.length)];
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof BlockstateComponentProperty.Entry) {
            this.blockProperty = ((BlockstateComponentProperty.Entry) entity).getProperty();
        }
    }

    @Getter
    public static class Storage implements EntityComponentStorage<GrowingComponent> {

        private String[] growTo;

        @Override
        public void constructTo(GrowingComponent component) {
            component.growTo = this.growTo;
        }

        @Override
        public void readJson(JsonObject json) {
            this.growTo = StreamUtils.stream(JSONUtils.getAsJsonArray(json, "grow_to"))
                    .filter(e -> e.isJsonPrimitive() && e.getAsJsonPrimitive().isString())
                    .map(e -> e.getAsJsonPrimitive().getAsString())
                    .toArray(String[]::new);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("grow_to", Arrays.stream(this.growTo).map(JsonPrimitive::new).collect(CollectorUtils.toJsonArray()));
        }

        public Storage setGrowTo(String... growTo) {
            this.growTo = growTo;
            return this;
        }
    }
}
