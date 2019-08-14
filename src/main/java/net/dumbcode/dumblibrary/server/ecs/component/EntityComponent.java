package net.dumbcode.dumblibrary.server.ecs.component;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public interface EntityComponent {
    default NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    default void deserialize(NBTTagCompound compound) {

    }

    default void serialize(ByteBuf buf) {
    }

    default void deserialize(ByteBuf buf) {
    }
}
