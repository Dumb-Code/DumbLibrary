package net.dumbcode.dumblibrary.server.ecs.item.components;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;


@Getter
@Setter
public class ItemRenderModelComponent extends EntityComponent {

    private static final String MODEL_KEY = "model_location";

    private ResourceLocation location = DumbLibrary.MODEL_MISSING; //MRL locations are in the form `namespace:path#varient

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString(MODEL_KEY, this.location.toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.location = new ResourceLocation(compound.getString(MODEL_KEY));
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<ItemRenderModelComponent> {

        private ResourceLocation location = DumbLibrary.MODEL_MISSING;

        @Override
        public ItemRenderModelComponent constructTo(ItemRenderModelComponent component) {
            component.location = this.location;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.location = new ResourceLocation(JsonUtils.getString(json, MODEL_KEY));
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty(MODEL_KEY, this.location.toString());
        }
    }
}
