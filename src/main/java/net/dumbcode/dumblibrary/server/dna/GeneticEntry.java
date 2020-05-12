package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

@Data
@Accessors(chain = true)

public class GeneticEntry<T extends GeneticFactoryStorage> {
    private final GeneticType<T> type;
    private final String identifier;
    @Nullable private final T storage;
    private final float baseValue;
    private final float modifierRange;
    private float modifier = 0; //Normal distribution with mean 1

    private GeneticEntry(GeneticEntry<T> schemaEntry) {
        this(schemaEntry.getType(), schemaEntry.getIdentifier(), schemaEntry.getStorage(), schemaEntry.getBaseValue(), schemaEntry.getModifierRange());
    }

    public GeneticEntry(GeneticType<T> type, String identifier, T storage, float baseValue, float modifierRange) {
        this.type = type;
        this.identifier = identifier;
        this.storage = storage;
        this.baseValue = baseValue;
        this.modifierRange = modifierRange;
    }

    public GeneticEntry<T> copy() {
        return new GeneticEntry<>(this);
    }

    public GeneticEntry<T> setRandomModifier() {
        this.modifier = (float) new Random().nextGaussian();
        return this;
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("identifier", this.identifier);
        compound.setString("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        compound.setFloat("base_value", this.baseValue);
        compound.setFloat("modifier_range", this.modifierRange);
        compound.setFloat("modifier", this.modifier);
        JavaUtils.nullApply(this.storage, t -> compound.setTag("storage", t.serialize(new NBTTagCompound())));
        return compound;
    }

    public JsonObject serialize(JsonObject json) {
        json.addProperty("identifier", this.identifier);
        json.addProperty("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        json.addProperty("base_value", this.baseValue);
        json.addProperty("modifier_range", this.modifierRange);
        json.addProperty("modifier", this.modifier);
        JavaUtils.nullApply(this.storage, t -> json.add("storage", t.serialize(new JsonObject())));
        return json;
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry<T> deserialize(NBTTagCompound compound) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(compound.getString("type"))));
        return new GeneticEntry<>(
            type,
            compound.getString("identifier"),
            JavaUtils.nullApply(type.getStorage().get(), t -> t.deserialize(compound.getCompoundTag("storage"))),
            compound.getFloat("base_value"),
            compound.getFloat("modifier_range")
        ).setModifier(compound.getFloat("modifier"));
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry deserialize(JsonObject json) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(json, "type"))));
        return new GeneticEntry<>(
            type,
            JsonUtils.getString(json, "identifier"),
            JavaUtils.nullApply(type.getStorage().get(), t -> t.deserialize(JsonUtils.getJsonObject(json, "storage"))),
            JsonUtils.getFloat(json, "base_value"),
            JsonUtils.getFloat(json, "modifier_range")
        ).setModifier(JsonUtils.getFloat(json, "modifier"));
    }
}
