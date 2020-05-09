package net.dumbcode.dumblibrary.server.attributes;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setDouble("base_value", this.baseValue);

        NBTTagList nbtOperations = new NBTTagList();
        this.operations.forEach((uuid, modifier) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setUniqueId("uuid", uuid);
            compound.setInteger("modifier", modifier.getOp().ordinal());
            compound.setDouble("value", modifier.getValue());
            nbtOperations.appendTag(compound);
        });
        nbt.setTag("operations", nbtOperations);

        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.baseValue = nbt.getDouble("base_value");
        this.operations.clear();

        for (NBTBase base : nbt.getTagList("operations", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound compound = (NBTTagCompound) base;
            UUID uuid = compound.getUniqueId("uuid");
            this.operations.put(uuid, new ModifiableFieldModifier(uuid, ModOp.values()[compound.getInteger("modifier")], compound.getDouble("value")));
        }
    }

    public static ModifiableField createField(double baseValue) {
        ModifiableField field = new ModifiableField();
        field.setBaseValue(baseValue);
        return field;
    }

}
