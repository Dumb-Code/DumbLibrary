package net.dumbcode.dumblibrary.server.dna;

public interface GeneticValueApplier<S extends GeneticFactoryStorage<O>, T, O> {
    void apply(O value, T type, S storage);
}
