package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum ImageBufferHandler implements BiConsumer<PacketBuffer, DummyImage>, Function<PacketBuffer, DummyImage> {
    INSTANCE;

    public void serialize(PacketBuffer buf, DummyImage image) {
        byte[] data = image.getData();
        buf.writeInt(data.length);
        buf.writeByteArray(data);
    }

    public DummyImage deserialize(PacketBuffer buf) {
        return new DummyImage(buf.readByteArray(buf.readInt()));
    }

    @Override
    public void accept(PacketBuffer buf, DummyImage image) {
        this.serialize(buf, image);
    }

    @Override
    public DummyImage apply(PacketBuffer buf) {
        return this.deserialize(buf);
    }
}