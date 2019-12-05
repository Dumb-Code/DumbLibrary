package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraft.nbt.NBTTagCompound;

public interface SaveableEntityStorage<T extends EntityComponent> extends EntityComponentStorage<T> {

    default void readNBT(NBTTagCompound nbt) {

    }

    default NBTTagCompound writeNBT(NBTTagCompound nbt){
        return nbt;
    }

}
