package net.dumbcode.dumblibrary.server.dna;

public interface GeneticValueApplier<S extends GeneticFactoryStorage, T, O> {
    void apply(O value, T type, S storage);
}
