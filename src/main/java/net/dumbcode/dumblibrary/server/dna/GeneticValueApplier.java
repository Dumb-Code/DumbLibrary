package net.dumbcode.dumblibrary.server.dna;

public interface GeneticValueApplier<S extends GeneticFactoryStorage, T> {
    void apply(double rawValue, T type, S storage);
}
