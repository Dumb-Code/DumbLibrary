package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.util.math.MathHelper;

public class GeneticUtils {

    public static int decodeFloatColorInt(float value) {
        return Float.floatToRawIntBits(value);
    }

    public static float[] decodeFloatColor(float value) {
        int actValue = decodeFloatColorInt(value);
        float red =   ((actValue     ) & 255) / 255F;
        float green = ((actValue >> 8) & 255) / 255F;
        float blue =  ((actValue >> 16) & 255) / 255F;
        float alpha =  ((actValue >> 24) & 255) / 255F;
        return new float[] {red, green, blue, alpha};
    }

    public static float encodeFloatColor(float r, float g, float b, float a) {
        return Float.intBitsToFloat(encode(r) | encode(g) << 8 | encode(b) << 16 | encode(a) << 24);
    }

    private static int encode(float v) {
        return (int) MathHelper.clamp(v * 255, 0, 255);
    }
}
