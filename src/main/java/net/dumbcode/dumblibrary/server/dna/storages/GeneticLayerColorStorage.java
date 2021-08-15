package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticLayerColorStorage extends GeneticColorStorage {

    private String layerName = "undefined";
    private boolean primary = true;

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putString("layer", this.layerName);
        nbt.putBoolean("primary", this.primary);
        return nbt;
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.layerName = nbt.getString("layer");
        this.primary = nbt.getBoolean("primary");
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("layer", this.layerName);
        json.addProperty("primary", this.primary);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.layerName = JSONUtils.getAsString(json, "layer");
        this.primary = JSONUtils.getAsBoolean(json, "primary");
    }

    @Override
    public Object getCombinerKey() {
        return this.layerName;
    }
}
