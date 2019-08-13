package net.dumbcode.dumblibrary.server.ecs.blocks.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public class GrowingComponent implements EntityComponent {

    private static final String GROW_TO_KEY = "grow_to";

    @Getter private String growTo;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString(GROW_TO_KEY, this.growTo);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.growTo = compound.getString(GROW_TO_KEY);
    }

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
            this.growTo = JsonUtils.getString(json, GROW_TO_KEY);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty(GROW_TO_KEY, this.growTo);
        }
    }

}
