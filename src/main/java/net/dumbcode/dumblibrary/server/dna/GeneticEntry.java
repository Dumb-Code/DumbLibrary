package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)

public class GeneticEntry<T extends GeneticFactoryStorage> {
    private final GeneticType<T> type;
    private final String identifier;
    @Nullable private final T storage;
    private final float baseValue;
    private final float modifierRange;
    private float modifier = 0; //Normal distribution with mean 1. This is stored as an int, to preserve accuracy over NAN

    private GeneticEntry(GeneticEntry<T> schemaEntry) {
        this(schemaEntry.getType(), schemaEntry.getIdentifier(), schemaEntry.getStorage(), schemaEntry.getBaseValue(), schemaEntry.getModifierRange());
    }

    public GeneticEntry(Supplier<? extends GeneticType<T>> type, String identifier, T storage, float baseValue, float modifierRange) {
        this(type.get(), identifier, storage, baseValue, modifierRange);
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

    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putString("identifier", this.identifier);
        compound.putString("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        compound.putFloat("base_value", this.baseValue);
        compound.putFloat("modifier_range", this.modifierRange);
        compound.putInt("modifier", Float.floatToRawIntBits(this.modifier));
        JavaUtils.nullApply(this.storage, t -> compound.put("storage", t.serialize(new CompoundNBT())));
        return compound;
    }

    public JsonObject serialize(JsonObject json) {
        json.addProperty("identifier", this.identifier);
        json.addProperty("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        json.addProperty("base_value", this.baseValue);
        json.addProperty("modifier_range", this.modifierRange);
        json.addProperty("modifier", Float.floatToRawIntBits(this.modifier));
        JavaUtils.nullApply(this.storage, t -> json.add("storage", t.serialize(new JsonObject())));
        return json;
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry<T> deserialize(CompoundNBT compound) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(compound.getString("type"))));
        return new GeneticEntry<>(
            type,
            compound.getString("identifier"),
            JavaUtils.nullApply(type.getStorage().get(), t -> t.deserialize(compound.getCompound("storage"))),
            compound.getFloat("base_value"),
            compound.getFloat("modifier_range")
        ).setModifier(Float.intBitsToFloat(compound.getInt("modifier")));
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry deserialize(JsonObject json) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(JSONUtils.getAsString(json, "type"))));
        return new GeneticEntry<>(
            type,
            JSONUtils.getAsString(json, "identifier"),
            JavaUtils.nullApply(type.getStorage().get(), t -> t.deserialize(JSONUtils.getAsJsonObject(json, "storage"))),
            JSONUtils.getAsFloat(json, "base_value"),
            JSONUtils.getAsFloat(json, "modifier_range")
        ).setModifier((Float.intBitsToFloat(JSONUtils.getAsInt(json, "modifier"))));
    }
}
