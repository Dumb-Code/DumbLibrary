package net.dumbcode.dumblibrary.server.dna.storages;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

@Getter
public abstract class RandomUUIDStorage<O> implements GeneticFactoryStorage<O> {
    private UUID randomUUID = UUID.randomUUID();

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        nbt.putUUID("uuid", this.randomUUID);
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
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
