package net.dumbcode.dumblibrary.server.dna;

public interface GeneticValueApplier<S extends GeneticFactoryStorage, T> {
    void apply(float value, float rawValue, T type, S storage);
}
