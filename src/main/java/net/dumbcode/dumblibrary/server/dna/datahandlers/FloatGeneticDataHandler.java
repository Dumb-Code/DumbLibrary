package net.dumbcode.dumblibrary.server.dna.datahandlers;

public enum FloatGeneticDataHandler implements GeneticDataHandler {
    INSTANCE;

    @Override
    public float combine(float a, float b) {
        return (a + b) / 2;
    }

    @Override
    public float gaussian(float mean, float range) {
        return (float) (mean + range*RAND.nextGaussian());
    }
}
