package net.dumbcode.dumblibrary.server.attributes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.IntStream;

public class ModifiableField {
    private double baseValue;
    private double cachedValue;
    private boolean needsUpdate = true;

    private final Map<UUID, ModifiableFieldModifier> operations = new HashMap<>();

    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
        this.needsUpdate = true;
    }

    public double getValue() {
        if (this.needsUpdate) {
            this.cachedValue = this.computeValue();
            this.needsUpdate = false;
        }

        return this.cachedValue;
    }

    public int getIntValue() {
        return (int) this.getValue();
    }

    public void addModifier(UUID uuid, double value) {
        this.addModifier(new ModifiableFieldModifier(uuid, value));
    }

    public void addModifier(ModifiableFieldModifier modifier) {
        ModifiableFieldModifier old = this.operations.put(modifier.getUuid(), modifier);
        if(!modifier.equals(old)) {
            this.needsUpdate = true;
        }
    }

    public void removeModifier(UUID uuid) {
        if (this.operations.remove(uuid) != null) {
            this.needsUpdate = true;
        }
    }

    private double computeValue() {
        double startValue = this.baseValue;
        double result = startValue;
        for (ModifiableFieldModifier value : this.operations.values()) {
            result += startValue * value.getValue();
        }
        return result;
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putDouble("base_value", this.baseValue);

        ListNBT nbtOperations = new ListNBT();
        this.operations.forEach((uuid, modifier) -> {
            CompoundTag compound = new CompoundTag();
            compound.putUUID("uuid", uuid);
            compound.putDouble("value", modifier.getValue());
            nbtOperations.add(compound);
        });
        nbt.put("operations", nbtOperations);

        return nbt;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeDouble(this.baseValue);
        buffer.writeShort(this.operations.size());
        this.operations.forEach((uuid, modifier) -> {
            buffer.writeUUID(uuid);
            buffer.writeDouble(modifier.getValue());
        });
    }

    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.baseValue = buffer.readDouble();
        this.operations.clear();
        IntStream.range(0, buffer.readShort()).forEach(i -> {
            UUID uuid = buffer.readUUID();
            this.operations.put(uuid, new ModifiableFieldModifier(uuid, buffer.readDouble()));
        });
    }

    public void readFromNBT(CompoundTag nbt) {
        this.baseValue = nbt.getDouble("base_value");
        this.operations.clear();

        for (INBT base : nbt.getList("operations", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) base;
            UUID uuid = compound.getUUID("uuid");
            this.operations.put(uuid, new ModifiableFieldModifier(uuid, compound.getDouble("value")));
        }
    }

    public static ModifiableField createField(double baseValue) {
        ModifiableField field = new ModifiableField();
        field.setBaseValue(baseValue);
        return field;
    }

}
