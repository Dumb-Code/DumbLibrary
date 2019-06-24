package net.dumbcode.dumblibrary.server.entity.component;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public interface EntityComponent {
    NBTTagCompound serialize(NBTTagCompound compound);

    void deserialize(NBTTagCompound compound);

    default void serialize(ByteBuf buf) {
    }

    default void deserialize(ByteBuf buf) {
    }
}
