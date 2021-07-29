package net.dumbcode.dumblibrary.server.dna;

import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticLayerColorStorage;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeOverallTintStorage;

public class DefaultGeneticFactoryStorageTypes {
    public static final GeneticFactoryStorageType<GeneticFieldModifierStorage> MODIFIER = new GeneticFactoryStorageType<GeneticFieldModifierStorage>() {};
    public static final GeneticFactoryStorageType<GeneticLayerColorStorage> LAYER_COLOUR = new GeneticFactoryStorageType<GeneticLayerColorStorage>() {
        @Override
        public Object combinerKey(GeneticLayerColorStorage geneticLayerColorStorage) {
            return geneticLayerColorStorage.getLayerName();
        }
    };
    public static final GeneticFactoryStorageType<GeneticTypeOverallTintStorage> OVERALL_COLOUR = new GeneticFactoryStorageType<GeneticTypeOverallTintStorage>() {
        @Override
        public Object combinerKey(GeneticTypeOverallTintStorage geneticTypeOverallTintStorage) {
            return geneticTypeOverallTintStorage.getTintType();
        }
    };
}
