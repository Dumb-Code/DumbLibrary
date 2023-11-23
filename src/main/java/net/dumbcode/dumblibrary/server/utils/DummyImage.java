package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DummyImage {

    public static final DummyImage EMPTY = new DummyImage(new byte[0]);

    private byte[] data;

    @OnlyIn(Dist.CLIENT)
    private NativeImage image;

    public DummyImage(byte[] data) {
        this.data = data;
    }

    @OnlyIn(Dist.CLIENT)
    public NativeImage getImage() {
        if(this.image == null) {
            try {
                this.image = NativeImage.read(new ByteArrayInputStream(this.data));
            } catch (IOException e) {
                DumbLibrary.getLogger().warn("Unable to read image", e);
                this.image = MissingTextureSprite.getTexture().getPixels();
            }
        }
        return this.image;
    }

    public byte[] getData() {
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            if(this.data == null) throw new IllegalArgumentException();
        } else if(this.data == null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    this.data = this.image.asByteArray();
                } catch (IOException e) {
                    DumbLibrary.getLogger().error("Unable to make image byte array", e);
                }
            });
        }
        return data;
    }

    @OnlyIn(Dist.CLIENT)
    public static DummyImage of(NativeImage image) {
        DummyImage dummyImage = new DummyImage(new byte[0]);
        dummyImage.image = image;
        return dummyImage;
    }

    public static DummyImage read(InputStream stream) throws IOException {
        byte[] arr = new byte[stream.available()];
        stream.read(arr);
        return new DummyImage(arr);
    }
}
