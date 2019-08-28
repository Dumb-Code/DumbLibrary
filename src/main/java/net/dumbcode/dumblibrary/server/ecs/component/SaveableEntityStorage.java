package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraft.nbt.NBTTagCompound;

public interface SaveableEntityStorage<T extends EntityComponent> extends EntityComponentStorage<T> {

    T constructTo(T component);

    default void readNBT(NBTTagCompound nbt) {

    }

    default NBTTagCompound writeNBT(NBTTagCompound nbt){
        return nbt;
    }

}
