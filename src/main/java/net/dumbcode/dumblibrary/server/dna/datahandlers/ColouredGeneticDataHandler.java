package net.dumbcode.dumblibrary.server.dna.datahandlers;

import net.dumbcode.dumblibrary.server.utils.GeneticUtils;

public enum ColouredGeneticDataHandler implements GeneticDataHandler {
    INSTANCE;

    @Override
    public float combine(float a, float b) {
        float[] argb = GeneticUtils.decodeFloatColor(a);
        float[] brgb = GeneticUtils.decodeFloatColor(b);
        return GeneticUtils.encodeFloatColor((argb[0] + brgb[0]) / 2, (argb[1] + brgb[1]) / 2, (argb[2] + brgb[2]) / 2,  (argb[3] + brgb[3]) / 2);
    }

    @Override
    public float gaussian(float mean, float range) {
        return GeneticUtils.encodeFloatColor(
            (float)(mean + RAND.nextGaussian()*range),
            (float)(mean + RAND.nextGaussian()*range),
            (float)(mean + RAND.nextGaussian()*range),
            (float)(mean + RAND.nextGaussian()*range)
        );
    }
}
