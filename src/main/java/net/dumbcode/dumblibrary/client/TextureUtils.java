package net.dumbcode.dumblibrary.client;

import lombok.Cleanup;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResource;
import net.minecraft.util.HTTPUtil;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.*;

public class TextureUtils {

    private static final Minecraft MC = Minecraft.getInstance();

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

                NativeImage[] images = loadImages(locations);

                NativeImage largestImage = Arrays.stream(images).max(Comparator.comparing(NativeImage::getWidth)).orElseThrow(IOException::new);
                int width = largestImage.getWidth();
                int height = largestImage.getHeight();
                int[][] imageData = new int[locations.length][];

                DynamicTexture outTexture = new DynamicTexture(width, height, true);
                ResourceLocation location = Minecraft.getInstance().getTextureManager().register("dumblib_multitexture", outTexture);

                HTTPUtil.DOWNLOAD_EXECUTOR.execute(() -> {
                    for (int i = 0; i < images.length; i++) {
                        imageData[i] = new int[width * height];
                        NativeImage resized = resize(images[i], width, height);
                        imageData[i] = resized.makePixelArray();
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
                        outTexture.getId(), location
                    );

//                    //Optifine does something weird with the int[] data, so it's easier to make a dummy texture and transfer the pixels over
//                    Minecraft.getInstance().execute(() -> {
//                        int[] pixels = createImage(width, height, overlappedData).getPixels();
//                        System.arraycopy(pixels, 0, outTexture.getPixels(), 0, pixels.length);
//                        outTexture.updateDynamicTexture();
//                    });

                });
                return location;
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to combine layers: " + Arrays.toString(locations), e);
                return TextureManager.INTENTIONAL_MISSING_TEXTURE;
            }
        });
    }

//    private static DynamicTexture createImage(int width, int height, int[] data) {
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        image.setRGB(0, 0, width, height, data, 0, width);
//        return new DynamicTexture(NativeImage.read());
//    }

    private static NativeImage[] loadImages(ResourceLocation[] locations) throws IOException {
        NativeImage[] images = new NativeImage[locations.length];
        float ratio = -1;
        for (int i = 0; i < locations.length; i++) {
            @Cleanup IResource resource = MC.getResourceManager().getResource(locations[i]);
            NativeImage image = NativeImage.read(TextureUtil.readResource(resource.getInputStream()));

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

    //Copied from:
    //https://github.com/accord-net/java/blob/master/Catalano.Image/src/Catalano/Imaging/Filters/ResizeBicubic.java#L170
    public static NativeImage resize(NativeImage img, int newWidth, int newHeight) {
        if(img.getWidth() != newWidth || img.getHeight() != newHeight) {
            NativeImage resized = new NativeImage(img.format(), newWidth, newHeight, false);

            int width = img.getWidth();
            int height = img.getHeight();
            double jFactor = (double)width / (double)newWidth;
            double iFactor = (double)height / (double)newHeight;

            // coordinates of source points
            double  ox, oy, dx, dy, k1, k2;
            int     ox1, oy1, ox2, oy2;

            // width and height decreased by 1
            int imax = height - 1;
            int jmax = width - 1;

            for (int i = 0; i < newHeight; i++) {

                // Y coordinates
                oy  = (double) i * iFactor - 0.5;
                oy1 = (int) oy;
                dy  = oy - (double) oy1;

                for (int j = 0; j < newWidth; j++) {

                    // X coordinates
                    ox  = (double) j * jFactor - 0.5f;
                    ox1 = (int) ox;
                    dx  = ox - (double) ox1;

                    int r, g, b, a;
                    r = g = b = a = 0;

                    for ( int n = -1; n < 3; n++ ){

                        // get Y cooefficient
                        k1 = BiCubicKernel( dy - (double) n );

                        oy2 = oy1 + n;
                        if ( oy2 < 0 )
                            oy2 = 0;
                        if ( oy2 > imax )
                            oy2 = imax;

                        for ( int m = -1; m < 3; m++ ){

                            // get X cooefficient
                            k2 = k1 * BiCubicKernel( (double) m - dx );

                            ox2 = ox1 + m;
                            if ( ox2 < 0 )
                                ox2 = 0;
                            if ( ox2 > jmax )
                                ox2 = jmax;

                            int color = img.getPixelRGBA(oy2, ox2);
                            a += k2 * ((color >> 24) & 0xFF);
                            r += k2 * ((color >> 16) & 0xFF);
                            g += k2 * ((color >> 8) & 0xFF);
                            b += k2 * (color & 0xFF);
                        }
                    }

                    a = Math.max( 0, Math.min( 255, a ) );
                    r = Math.max( 0, Math.min( 255, r ) );
                    g = Math.max( 0, Math.min( 255, g ) );
                    b = Math.max( 0, Math.min( 255, b ) );

                    resized.setPixelRGBA(j, i, (a << 24) | (r << 16) | (g << 8) | (b));
                }
            }
            return resized;
        }
        return img;
    }

    private static double BiCubicKernel( double x ){
        if ( x < 0 )
        {
            x = -x;
        }

        double biCoef = 0;

        if ( x <= 1 )
        {
            biCoef = ( 1.5 * x - 2.5 ) * x * x + 1;
        }
        else if ( x < 2 )
        {
            biCoef = ( ( -0.5 * x + 2.5 ) * x - 4 ) * x + 2;
        }

        return biCoef;
    }
}
