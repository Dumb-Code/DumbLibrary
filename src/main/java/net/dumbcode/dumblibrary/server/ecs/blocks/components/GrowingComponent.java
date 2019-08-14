package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.util.JsonUtils;

public class GrowingComponent implements EntityComponent {

    @Getter private String growTo;

    @Accessors(chain = true)
    @Setter
    @Getter
    public static class Storage implements EntityComponentStorage<GrowingComponent> {

        private String growTo;

        @Override
        public GrowingComponent construct() {
            GrowingComponent component = new GrowingComponent();
            component.growTo = this.growTo;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.growTo = JsonUtils.getString(json, "grow_to");
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("grow_to", this.growTo);
        }
    }
}
