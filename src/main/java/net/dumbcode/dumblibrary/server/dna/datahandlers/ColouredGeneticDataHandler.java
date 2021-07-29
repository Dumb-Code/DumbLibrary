package net.dumbcode.dumblibrary.server.dna.datahandlers;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;

public enum ColouredGeneticDataHandler implements GeneticDataHandler {
    INSTANCE;

    @Override
    public double combineChild(double a, double b) {
        float[] argb = GeneticUtils.decodeFloatColor(a);
        float[] brgb = GeneticUtils.decodeFloatColor(b);
        return GeneticUtils.encodeFloatColor((argb[0] + brgb[0]) / 2, (argb[1] + brgb[1]) / 2, (argb[2] + brgb[2]) / 2,  (argb[3] + brgb[3]) / 2, (int) ((argb[4] + brgb[4]) / 2));
    }

    @Override
    public double combineMultipleSources(DoubleList floats) {
        float[] total = new float[4];
        int size = 0;
        for (Double aFloat : floats) {
            float[] rgb = GeneticUtils.decodeFloatColor(aFloat);
            int importance = Math.max((int) rgb[4], 1);
            total[0] += rgb[0] * importance;
            total[1] += rgb[1] * importance;
            total[2] += rgb[2] * importance;
            total[3] += rgb[3] * importance;
            size += importance;
        }
        return GeneticUtils.encodeFloatColor(total[0] / size, total[1] / size, total[2] / size, total[3] / size);
    }

    @Override
    public double scale(double value, double modifier) {
        float[] rgb = GeneticUtils.decodeFloatColor(value);
        return GeneticUtils.encodeFloatColor(rgb[0], rgb[1], rgb[2], rgb[3], (int) (rgb[4] * modifier));
    }
}
