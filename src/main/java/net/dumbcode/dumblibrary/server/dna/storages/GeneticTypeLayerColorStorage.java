package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticTypeLayerColorStorage extends GeneticColorStorage {

    private String layerName = "undefined";

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putString("layer", this.layerName);
        return nbt;
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.layerName = nbt.getString("layer");
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("layer", this.layerName);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.layerName = JSONUtils.getAsString(json, "layer");
    }
}
