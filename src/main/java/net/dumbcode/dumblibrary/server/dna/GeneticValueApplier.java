package net.dumbcode.dumblibrary.server.dna;

public interface GeneticValueApplier<S extends GeneticFactoryStorage, T> {
    void apply(float value, int rawValue, T type, S storage);
}
