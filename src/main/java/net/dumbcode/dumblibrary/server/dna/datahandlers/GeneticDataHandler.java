package net.dumbcode.dumblibrary.server.dna.datahandlers;

import java.util.Random;

public interface GeneticDataHandler {
    Random RAND = new Random();

    float combine(float a, float b);
    float gaussian(float mean, float range);
}
