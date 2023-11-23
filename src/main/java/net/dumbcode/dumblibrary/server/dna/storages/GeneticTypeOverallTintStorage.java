package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JSONUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticTypeOverallTintStorage extends GeneticColorStorage {

    private TintType tintType = TintType.TARGET;

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        nbt.putInt("TintType", this.tintType.ordinal());
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        this.tintType = TintType.values()[nbt.getInt("TintType") % TintType.values().length];
        super.deserialize(nbt);
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("tint_type", this.tintType.ordinal());
        return super.serialize(json);
    }

    @Override
    public void deserialize(JsonObject json) {
        this.tintType = TintType.values()[JSONUtils.getAsInt(json, "tint_type", 0) % TintType.values().length];
        super.deserialize(json);
    }

    @Override
    public Object getCombinerKey() {
        return this.tintType;
    }

    public enum TintType {
        DIRECT, TARGET;
    }

}
