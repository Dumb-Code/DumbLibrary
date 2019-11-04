package net.dumbcode.dumblibrary.client;

import com.sun.imageio.plugins.common.ImageUtil;
import lombok.Cleanup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TextureUtils {

    private static final Minecraft MC = Minecraft.getMinecraft();

    public static ResourceLocation generateMultipleTexture(ResourceLocation... locations) throws IOException {
        if(locations.length == 0) {
            throw new IOException("Input locations should not be len 0");
        }
        if(locations.length == 1) {
            return locations[0];
        }

        float ratio = -1;

        BufferedImage[] images = new BufferedImage[locations.length];
        for (int i = 0; i < locations.length; i++) {
            @Cleanup IResource resource = MC.getResourceManager().getResource(locations[i]);
            BufferedImage image = TextureUtil.readBufferedImage(resource.getInputStream());

            int width = image.getWidth();
            int height = image.getHeight();

            float imageRatio = width / (float) height;

            if(ratio == -1) {
                ratio = imageRatio;
            }
            if(ratio != imageRatio) {
                throw new IOException("Image at " + locations[i] + " was not the specified ratio '1:" + ratio + "'. Instead was: '" + width + " x " + height + "' (1:" + imageRatio + ")");
            }

            images[i] = image;
        }
        int width = Arrays.stream(images).mapToInt(BufferedImage::getWidth).max().orElseThrow(IOException::new);
        int height = (int) (width / ratio);


        int[][] imageData = new int[locations.length][];

        for (int i = 0; i < images.length; i++) {
            imageData[i] = new int[width * height];
            resize(images[i], width, height).getRGB(0, 0, width, height, imageData[i], 0, width);
        }

        int imageLength = imageData[0].length;

        DynamicTexture texture = new DynamicTexture(width, height);
        int[] overlappedData = texture.getTextureData();
        System.arraycopy(imageData[0], 0, overlappedData, 0, overlappedData.length);

        for (int i = 1; i < imageData.length; i++) {
            for (int d = 0; d < imageLength; d++) {
                overlappedData[d] = blend(imageData[i][d], overlappedData[d]);
            }
        }
        texture.updateDynamicTexture();
        return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dumblib_multitexture", texture);
    }

    private static int blend(int src, int dest) {
        //https://en.wikipedia.org/wiki/Alpha_compositing#Alpha_blending

        float srcA = ((src >> 24) & 255) / 255F;
        float destA =((dest >> 24) & 255) / 255F;

        float outA = srcA + destA*(1F - srcA);

        if(outA == 0) {
            return 0;
        }

        float srcR = ((src >> 16) & 255) / 255F;
        float srcG = ((src >> 8 ) & 255) / 255F;
        float srcB = ((src      ) & 255) / 255F;

        float destR =((dest >> 16) & 255) / 255F;
        float destG =((dest >> 8 ) & 255) / 255F;
        float destB =((dest      ) & 255) / 255F;

        float outR = (srcR*srcA + destR*destA*(1-srcA)) / outA;
        float outG = (srcG*srcA + destG*destA*(1-srcA)) / outA;
        float outB = (srcB*srcA + destB*destA*(1-srcA)) / outA;

        return
            (int) (outA * 255) << 24 |
                (int) (outR * 255) << 16 |
                (int) (outG * 255) << 8 |
                (int) (outB * 255);
    }

    private static BufferedImage resize(BufferedImage img, int width, int height) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}
