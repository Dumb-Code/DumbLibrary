package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

@Getter
public abstract class RandomUUIDStorage implements GeneticFactoryStorage {
    private UUID randomUUID = UUID.randomUUID();

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putUUID("uuid", this.randomUUID);
        return nbt;
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.randomUUID = nbt.getUUID("uuid");
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
