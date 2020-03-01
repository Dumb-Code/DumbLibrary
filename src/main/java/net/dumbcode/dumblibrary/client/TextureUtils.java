package net.dumbcode.dumblibrary.client;

import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TextureUtils {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final Map<List<ResourceLocation>, ResourceLocation> TEXTURE_CACHE = new HashMap<>();

    public static ResourceLocation generateMultipleTexture(ResourceLocation... locations) {
        return TEXTURE_CACHE.computeIfAbsent(Arrays.asList(locations), rls -> {
            try {
                if(locations.length == 0) {
                    throw new IOException("Input locations should not be len 0");
                }
                if(locations.length == 1) {
                    return locations[0];
                }

                BufferedImage[] images = loadImages(locations);

                BufferedImage largestImage = Arrays.stream(images).max(Comparator.comparing(BufferedImage::getWidth)).orElseThrow(IOException::new);
                int width = largestImage.getWidth();
                int height = largestImage.getHeight();
                int[][] imageData = new int[locations.length][];

                DynamicTexture outTexture = new DynamicTexture(width, height);
                ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dumblib_multitexture", outTexture);

                HttpUtil.DOWNLOADER_EXECUTOR.execute(() -> {
                    for (int i = 0; i < images.length; i++) {
                        imageData[i] = new int[width * height];
                        resize(images[i], width, height).getRGB(0, 0, width, height, imageData[i], 0, width);
                    }

                    int imageLength = imageData[0].length;

                    int[] overlappedData = new int[width * height];
                    for (int[] imageDatum : imageData) {
                        for (int d = 0; d < imageLength; d++) {
                            overlappedData[d] = blend(imageDatum[d], overlappedData[d]);
                        }
                    }

                    DumbLibrary.getLogger().info("Combined layers: {} to texture(id={}, name={})",
                        Arrays.toString(Arrays.stream(locations).map(r -> {
                            String path = r.getPath();
                            String[] split = path.split("/");
                            return split[split.length - 1];
                        }).toArray()),
                        outTexture.getGlTextureId(), location
                    );

                    //Optifine does something weird with the int[] data, so it's easier to make a dummy texture and transfer the pixels over
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        int[] pixels = createImage(width, height, overlappedData).getTextureData();
                        System.arraycopy(pixels, 0, outTexture.getTextureData(), 0, pixels.length);
                        outTexture.updateDynamicTexture();
                    });
                });
                return location;
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to combine layers: " + Arrays.toString(locations), e);
                return TextureManager.RESOURCE_LOCATION_EMPTY;
            }
        });
    }

    private static DynamicTexture createImage(int width, int height, int[] data) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, data, 0, width);
        return new DynamicTexture(image);
    }

    private static BufferedImage[] loadImages(ResourceLocation[] locations) throws IOException {
        BufferedImage[] images = new BufferedImage[locations.length];
        float ratio = -1;
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
        return images;
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

    public static BufferedImage resize(BufferedImage img, int width, int height) {
        if(img.getWidth() != width || img.getHeight() != height) {
            Image tmp = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();
            return resized;
        }
        return img;
    }
}
