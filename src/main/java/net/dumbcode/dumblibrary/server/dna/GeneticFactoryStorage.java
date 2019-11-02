package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public interface GeneticFactoryStorage {
    NBTTagCompound serialize(NBTTagCompound nbt);
    void deserialize(NBTTagCompound nbt);

    JsonObject serialize(JsonObject json);
    void deserialize(JsonObject json);
}
