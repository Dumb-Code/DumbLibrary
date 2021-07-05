package net.dumbcode.dumblibrary.server.utils;

import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public enum ImageBufferHandler implements BiConsumer<PacketBuffer, NativeImage>, Function<PacketBuffer, NativeImage> {
    INSTANCE;

    public void serialize(PacketBuffer buf, NativeImage image) {
        byte[] data = new byte[0];
        try {
            @Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(image.asByteArray());
            gzos.close();
            data = baos.toByteArray();
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write image to bytes", e);
        }
        buf.writeInt(data.length);
        buf.writeByteArray(data);
    }

    public NativeImage deserialize(PacketBuffer buf) {
        try {
            byte[] data = buf.readByteArray(buf.readInt());
            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(data);
            @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
            return NativeImage.read(gzis);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to read image from bytes");
        }
        return MissingTextureSprite.getTexture().getPixels();
    }

    @Override
    public void accept(PacketBuffer buf, NativeImage image) {
        this.serialize(buf, image);
    }

    @Override
    public NativeImage apply(PacketBuffer buf) {
        return this.deserialize(buf);
    }
}