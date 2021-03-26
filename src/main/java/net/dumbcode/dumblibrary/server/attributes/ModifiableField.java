package net.dumbcode.dumblibrary.server.attributes;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.*;

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

    public void addModifer(UUID uuid, ModOp op, double value) {
        this.addModifier(new ModifiableFieldModifier(uuid, op, value));
    }

    public void addModifier(ModifiableFieldModifier modifier) {
        ModifiableFieldModifier old = this.operations.get(modifier.getUuid());
        this.operations.put(modifier.getUuid(), modifier);
        if(!modifier.equals(old)) {
            this.needsUpdate = true;
        }
    }

    private double computeValue() {
        Map<ModOp, Set<ModifiableFieldModifier>> map = new EnumMap<>(ModOp.class);
        double startValue = this.baseValue;

        for (ModifiableFieldModifier modifier : this.operations.values()) {
            map.computeIfAbsent(modifier.getOp(), op -> new HashSet<>()).add(modifier);
        }

        for (ModifiableFieldModifier modifier : map.getOrDefault(ModOp.ADD, new HashSet<>())) {
            startValue += modifier.getValue();
        }

        double result = startValue;

        for (ModifiableFieldModifier modifier : map.getOrDefault(ModOp.MULTIPLY_BASE_THEN_ADD, new HashSet<>())) {
            result += startValue * modifier.getValue();
        }

        for (ModifiableFieldModifier modifier : map.getOrDefault(ModOp.MULTIPLY, new HashSet<>())) {
            result *= modifier.getValue();
        }

        return result;
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putDouble("base_value", this.baseValue);

        ListNBT nbtOperations = new ListNBT();
        this.operations.forEach((uuid, modifier) -> {
            CompoundNBT compound = new CompoundNBT();
            compound.putUUID("uuid", uuid);
            compound.putInt("modifier", modifier.getOp().ordinal());
            compound.putDouble("value", modifier.getValue());
            nbtOperations.add(compound);
        });
        nbt.put("operations", nbtOperations);

        return nbt;
    }

    public void readFromNBT(CompoundNBT nbt) {
        this.baseValue = nbt.getDouble("base_value");
        this.operations.clear();

        for (INBT base : nbt.getList("operations", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT compound = (CompoundNBT) base;
            UUID uuid = compound.getUUID("uuid");
            this.operations.put(uuid, new ModifiableFieldModifier(uuid, ModOp.values()[compound.getInt("modifier")], compound.getDouble("value")));
        }
    }

    public static ModifiableField createField(double baseValue) {
        ModifiableField field = new ModifiableField();
        field.setBaseValue(baseValue);
        return field;
    }

}
