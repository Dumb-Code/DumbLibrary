package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum ImageBufferHandler implements BiConsumer<FriendlyByteBuf, DummyImage>, Function<FriendlyByteBuf, DummyImage> {
    INSTANCE;

    public void serialize(FriendlyByteBuf buf, DummyImage image) {
        byte[] data = image.getData();
        buf.writeInt(data.length);
        buf.writeByteArray(data);
    }

    public DummyImage deserialize(FriendlyByteBuf buf) {
        return new DummyImage(buf.readByteArray(buf.readInt()));
    }

    @Override
    public void accept(FriendlyByteBuf buf, DummyImage image) {
        this.serialize(buf, image);
    }

    @Override
    public DummyImage apply(FriendlyByteBuf buf) {
        return this.deserialize(buf);
    }
}