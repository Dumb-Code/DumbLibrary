package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraft.nbt.CompoundNBT;

public interface SaveableEntityStorage<T extends EntityComponent> extends EntityComponentStorage<T> {

    default void readNBT(CompoundNBT nbt) {

    }

    default CompoundNBT writeNBT(CompoundNBT nbt){
        return nbt;
    }

}
