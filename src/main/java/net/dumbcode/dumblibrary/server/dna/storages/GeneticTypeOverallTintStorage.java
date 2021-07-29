package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.DefaultGeneticFactoryStorageTypes;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorageType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticTypeOverallTintStorage extends GeneticColorStorage {

    private TintType tintType = TintType.DIRECT;

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putInt("TintType", this.tintType.ordinal());
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
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
    public GeneticFactoryStorageType<?> getType() {
        return DefaultGeneticFactoryStorageTypes.OVERALL_COLOUR;
    }

    public enum TintType {
        DIRECT, TARGET;
    }

}
