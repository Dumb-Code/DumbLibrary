package net.dumbcode.dumblibrary.server.utils;

public class GeneticUtils {

    public static float[] decode3BitColor(float value) {
        int actValue = (int) (MathUtils.bounce(0, 1F, value) * 512F);
        float red =   ((actValue     ) & 7) / 7F;
        float green = ((actValue >> 3) & 7) / 7F;
        float blue =  ((actValue >> 6) & 7) / 7F;
        return new float[] {red, green, blue};
    }

    public static float encode3BitColor(float r, float g, float b) {
        return ((int) (r * 7F) | (int) (g * 7F) << 3 | (int) (b * 7F) << 6) / 512F;
    }

}
