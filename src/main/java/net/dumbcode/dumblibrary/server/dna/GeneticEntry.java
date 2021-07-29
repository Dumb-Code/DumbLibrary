package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)

public class GeneticEntry<T extends GeneticFactoryStorage> {
    private final GeneticType<T> type;
    private final T storage;
    private double modifier = 0;

    private GeneticEntry(GeneticEntry<T> schemaEntry) {
        this(schemaEntry.getType(), schemaEntry.getStorage());
    }

    public GeneticEntry(Supplier<? extends GeneticType<T>> type, T storage) {
        this(type.get(), storage);
    }
    public GeneticEntry(GeneticType<T> type, @NonNull T storage) {
        this.type = type;
        this.storage = storage;
    }

    public GeneticEntry<T> copy() {
        return new GeneticEntry<>(this);
    }

    public GeneticEntry<T> setRandomModifier() {
        this.modifier = (float) new Random().nextGaussian();
        return this;
    }

    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putString("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        compound.putLong("modifier", Double.doubleToRawLongBits(this.modifier));
        JavaUtils.nullApply(this.storage, t -> compound.put("storage", t.serialize(new CompoundNBT())));
        return compound;
    }

    public JsonObject serialize(JsonObject json) {
        json.addProperty("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        json.addProperty("modifier", Double.doubleToRawLongBits(this.modifier));
        JavaUtils.nullApply(this.storage, t -> json.add("storage", t.serialize(new JsonObject())));
        return json;
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry<T> deserialize(CompoundNBT compound) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(compound.getString("type"))));
        return new GeneticEntry<>(
            type,
            Util.make(type.getStorage().get(), t -> t.deserialize(compound.getCompound("storage")))
        ).setModifier(Double.longBitsToDouble(compound.getInt("modifier")));
    }

    public static <T extends GeneticFactoryStorage> GeneticEntry deserialize(JsonObject json) {
        @SuppressWarnings("unchecked") GeneticType<T> type = (GeneticType<T>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(JSONUtils.getAsString(json, "type"))));
        return new GeneticEntry<>(
            type,
            Util.make(type.getStorage().get(), t -> t.deserialize(JSONUtils.getAsJsonObject(json, "storage")))
        ).setModifier((Double.longBitsToDouble(JSONUtils.getAsInt(json, "modifier"))));
    }
}
