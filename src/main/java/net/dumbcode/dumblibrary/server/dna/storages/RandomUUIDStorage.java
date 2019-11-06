package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

@Getter
public class RandomUUIDStorage implements GeneticFactoryStorage {
    private UUID randomUUID = UUID.randomUUID();

    @Override
    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setUniqueId("uuid", this.randomUUID);
        return nbt;
    }

    @Override
    public void deserialize(NBTTagCompound nbt) {
        this.randomUUID = nbt.getUniqueId("uuid");
    }

    @Override
    public JsonObject serialize(JsonObject json) {
        //Don't serialize to json
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {

    }
}
