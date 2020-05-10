package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticFieldModifierStorage extends RandomUUIDStorage {

    private ModOp operation;
    private float modifier = 1F;

    @Override
    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setInteger("Operation", this.operation.ordinal());
        nbt.setFloat("Modifier", this.modifier);
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(NBTTagCompound nbt) {
        this.operation = ModOp.values()[nbt.getInteger("Operation") % ModOp.values().length];
        this.modifier = nbt.getFloat("Modifier");
        super.deserialize(nbt);
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        json.addProperty("operation", this.operation.ordinal());
        json.addProperty("modifier", this.modifier);
        return super.serialize(json);
    }

    @Override
    public void deserialize(JsonObject json) {
        this.operation = ModOp.values()[JsonUtils.getInt(json, "operation", 0) % ModOp.values().length];
        this.modifier = JsonUtils.getFloat(json, "modifier", 1F);
        super.deserialize(json);
    }
}
