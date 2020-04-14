package net.dumbcode.dumblibrary.server.utils;

import io.netty.buffer.ByteBuf;
import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public enum ImageBufferHandler implements BiConsumer<ByteBuf, BufferedImage>, Function<ByteBuf, BufferedImage> {
    INSTANCE;

    public void serialize(ByteBuf buf, BufferedImage image) {
        byte[] data = new byte[0];
        try {
            @Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            ImageIO.write(image, "PNG", gzos);
            gzos.close();
            data = baos.toByteArray();
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to write image to bytes", e);
        }
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    public BufferedImage deserialize(ByteBuf buf) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        try {
            byte[] data = new byte[buf.readInt()];
            buf.readBytes(data);
            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(data);
            @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
            image = ImageIO.read(gzis);
        } catch (IOException e) {
            DumbLibrary.getLogger().error("Unable to read image from bytes");
        }
        return image;
    }

    @Override
    public void accept(ByteBuf buf, BufferedImage image) {
        this.serialize(buf, image);
    }

    @Override
    public BufferedImage apply(ByteBuf buf) {
        return this.deserialize(buf);
    }
}