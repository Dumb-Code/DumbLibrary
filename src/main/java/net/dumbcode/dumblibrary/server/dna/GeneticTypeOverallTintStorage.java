package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticTypeOverallTintStorage extends RandomUUIDStorage {

    private TintType tintType = TintType.DIRECT;

    @Override
    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setInteger("TintType", this.tintType.ordinal());
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(NBTTagCompound nbt) {
        this.tintType = TintType.values()[nbt.getInteger("TintType") % TintType.values().length];
        super.deserialize(nbt);
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("tint_type", this.tintType.ordinal());
        return super.serialize(json);
    }

    @Override
    public void deserialize(JsonObject json) {
        this.tintType = TintType.values()[JsonUtils.getInt(json, "tint_type", 0) % TintType.values().length];
        super.deserialize(json);
    }

    public enum TintType {
        DIRECT, TARGET;
    }

}
