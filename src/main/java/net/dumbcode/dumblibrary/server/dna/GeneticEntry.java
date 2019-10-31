package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

@Data
@Accessors(chain = true)
public class GeneticEntry {
    private final GeneticType type;
    private final float baseValue;
    private final float modifierRange;
    private int modifier = 128; //0 -> -1, 128 -> 0, 256 -> 1

    public GeneticEntry(GeneticEntry schemaEntry) {
        this(schemaEntry.getType(), schemaEntry.getBaseValue(), schemaEntry.getModifierRange());
    }

    public GeneticEntry(GeneticType type, float baseValue, float modifierRange) {
        this.type = type;
        this.baseValue = baseValue;
        this.modifierRange = modifierRange;
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        compound.setFloat("base_value", this.baseValue);
        compound.setFloat("modifier_range", this.modifierRange);
        compound.setInteger("modifier", this.modifier);
        return compound;
    }

    public JsonObject serialize(JsonObject json) {
        json.addProperty("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        json.addProperty("base_value", this.baseValue);
        json.addProperty("modifier_range", this.modifierRange);
        json.addProperty("modifier", this.modifier);
        return json;
    }

    public static GeneticEntry deseraize(NBTTagCompound compound) {
        return new GeneticEntry(
            Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(compound.getString("type")))),
            compound.getFloat("base_value"),
            compound.getFloat("modifier_range")
        ).setModifier(compound.getInteger("modifier"));
    }

    public static GeneticEntry deseraize(JsonObject json) {
        return new GeneticEntry(
            Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(json, "type")))),
            JsonUtils.getFloat(json, "base_value"),
            JsonUtils.getFloat(json, "modifier_range")
        ).setModifier(JsonUtils.getInt(json, "modifier"));
    }
}
