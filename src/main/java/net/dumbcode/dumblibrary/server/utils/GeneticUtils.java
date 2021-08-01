package net.dumbcode.dumblibrary.server.utils;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorageType;
import net.dumbcode.dumblibrary.server.dna.GeneticType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GeneticUtils {

    public static int DEFAULT_COLOUR_IMPORTANCE = 1000;

    public static <T extends GeneticFactoryStorage<?>> Object getCombinerKey(GeneticEntry<T, ?> entry) {
        @SuppressWarnings("unchecked")
        GeneticFactoryStorageType<T> type = (GeneticFactoryStorageType<T>) entry.getStorage().getType();
        return type.combinerKey(entry.getStorage());
    }

    public static List<GeneticEntry<?, ?>> combineAll(List<GeneticEntry<?, ?>> entries) {
        Map<GeneticType<?, ?>, Map<Object, List<GeneticEntry<?, ?>>>> geneticTypeMap = new HashMap<>();
        for (GeneticEntry<?, ?> entry : entries) {
            geneticTypeMap
                .computeIfAbsent(entry.getType(), t -> new HashMap<>())
                .computeIfAbsent(getCombinerKey(entry), k -> new ArrayList<>())
                .add(entry);
        }

        List<GeneticEntry<?, ?>> combined = new ArrayList<>();
        geneticTypeMap.forEach((geneticType, objectListMap) -> {
            for (List<GeneticEntry<?, ?>> toCombine : objectListMap.values()) {
                combineAndSetModifiers(geneticType, toCombine, combined);
            }
        });
        return combined;
    }

    private static <T extends GeneticFactoryStorage<O>, O> void combineAndSetModifiers(GeneticType<T, O> type, List<GeneticEntry<?, ?>> toCombine, List<GeneticEntry<?, ?>> combined) {
        List<O> datas = toCombine.stream()
            .map(e -> (GeneticEntry<T, O>) e)
            .map(GeneticEntry::getModifier)
            .collect(Collectors.toList());
        O o = type.getDataHandler().combineMultipleSources(datas);
        GeneticEntry<T, O> copy = (GeneticEntry<T, O>) toCombine.get(0).copy();
        combined.add(copy.setModifier(o));
    }
}
