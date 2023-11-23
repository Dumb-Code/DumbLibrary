package net.dumbcode.dumblibrary.server.ecs.component;

import net.minecraft.nbt.CompoundTag;

public interface SaveableEntityStorage<T extends EntityComponent> extends EntityComponentStorage<T> {

    default void readNBT(CompoundTag nbt) {

    }

    default CompoundTag writeNBT(CompoundTag nbt){
        return nbt;
    }

}
