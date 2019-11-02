package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticTypeLayerColorStorage implements GeneticFactoryStorage {

    private String layerName = "undefined";

    @Override
    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setString("layer", this.layerName);
        return nbt;
    }

    @Override
    public void deserialize(NBTTagCompound nbt) {
        this.layerName = nbt.getString("layer");
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("layer", this.layerName);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.layerName = JsonUtils.getString(json, "layer");
    }
}
