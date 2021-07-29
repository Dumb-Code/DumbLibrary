package net.dumbcode.dumblibrary.server.dna;

public interface GeneticFactoryStorageType<T extends GeneticFactoryStorage> {
    //All storages of this type, the same genetic type, and the same combiner key will be combined.
    default Object combinerKey(T t) {
        return null;
    }
}
