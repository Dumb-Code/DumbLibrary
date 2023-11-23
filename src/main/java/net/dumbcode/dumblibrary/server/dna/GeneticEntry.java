package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)
public class GeneticEntry<T extends GeneticFactoryStorage<O>, O> {
    private final GeneticType<T, O> type;
    private final T storage;
    private O modifier;

    private GeneticEntry(GeneticEntry<T, O> schemaEntry) {
        this(schemaEntry.getType(), schemaEntry.getStorage());
    }

    public GeneticEntry(GeneticType<T, O> type) {
        this(type, type.getStorage().get());
    }
    public GeneticEntry(Supplier<? extends GeneticType<T, O>> type, T storage) {
        this(type.get(), storage);
    }
    public GeneticEntry(GeneticType<T, O> type, @NonNull T storage) {
        this.type = type;
        this.storage = storage;
        this.modifier = this.type.getDataHandler().defaultValue();
    }

    public GeneticEntry<T, O> copy() {
        return new GeneticEntry<>(this);
    }

    public GeneticEntry<T, O> setRandomModifier(Random random) {
        this.modifier = this.type.getDataHandler().gaussianValue(random);
        return this;
    }

    public GeneticEntry<T, O> mutateModifier(Random random, float amount) {
        this.modifier = this.type.getDataHandler().mutateValue(this.modifier, random, amount);
        return this;
    }

    public IFormattableTextComponent gatherTextComponents() {
        return this.getType().getTranslationComponent().append(": ").append(this.type.getDataHandler().getValue(this.modifier));
    }

    public CompoundTag serialize(CompoundTag compound) {
        compound.putString("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        compound.put("value", this.type.getDataHandler().write(this.modifier, new CompoundTag()));
        JavaUtils.nullApply(this.storage, t -> compound.put("storage", t.serialize(new CompoundTag())));
        return compound;
    }

    public JsonObject serialize(JsonObject json) {
        json.addProperty("type", Objects.requireNonNull(this.type.getRegistryName()).toString());
        json.add("value", this.type.getDataHandler().write(this.modifier, new JsonObject()));
        JavaUtils.nullApply(this.storage, t -> json.add("storage", t.serialize(new JsonObject())));
        return json;
    }

    public static <T extends GeneticFactoryStorage<O>, O> GeneticEntry<T, O> deserialize(CompoundTag compound) {
        @SuppressWarnings("unchecked") GeneticType<T, O> type = (GeneticType<T, O>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(compound.getString("type"))));
        return new GeneticEntry<>(
            type,
            Util.make(type.getStorage().get(), t -> t.deserialize(compound.getCompound("storage")))
        ).setModifier(type.getDataHandler().read(compound.getCompound("value")));
    }

    public static <T extends GeneticFactoryStorage<O>, O> GeneticEntry<T, O> deserialize(JsonObject json) {
        @SuppressWarnings("unchecked") GeneticType<T, O> type = (GeneticType<T, O>) Objects.requireNonNull(DumbRegistries.GENETIC_TYPE_REGISTRY.getValue(new ResourceLocation(JSONUtils.getAsString(json, "type"))));
        return new GeneticEntry<>(
            type,
            Util.make(type.getStorage().get(), t -> t.deserialize(JSONUtils.getAsJsonObject(json, "storage")))
        ).setModifier(type.getDataHandler().read(json.getAsJsonObject("value")));
    }
}
