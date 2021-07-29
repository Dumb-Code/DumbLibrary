package net.dumbcode.dumblibrary.server.dna.datahandlers;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatList;

public interface GeneticDataHandler {

    double combineChild(double a, double b);

    double combineMultipleSources(DoubleList floats);

    double scale(double value, double modifier);
}
