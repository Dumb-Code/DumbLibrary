package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;

public interface GeneticFactoryStorage {
    CompoundNBT serialize(CompoundNBT nbt);
    void deserialize(CompoundNBT nbt);

    JsonObject serialize(JsonObject json);
    void deserialize(JsonObject json);
}
