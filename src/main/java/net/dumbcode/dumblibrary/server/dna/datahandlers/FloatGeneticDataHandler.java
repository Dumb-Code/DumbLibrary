package net.dumbcode.dumblibrary.server.dna.datahandlers;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public enum FloatGeneticDataHandler implements GeneticDataHandler {
    INSTANCE;

    @Override
    public double combineChild(double a, double b) {
        return (a + b) / 2;
    }

    @Override
    public double combineMultipleSources(DoubleList floats) {
        double sum = 0;
        for (Double f : floats) {
            sum += f;
        }
        return sum;
    }

    @Override
    public double scale(double value, double modifier) {
        return value * modifier;
    }
}
