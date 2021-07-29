package net.dumbcode.dumblibrary.server.utils;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorageType;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GeneticUtils {

    public static int decodeFloatColorInt(double value) {
        float[] floats = decodeFloatColor(value);
        int r = (int) (floats[0] * 255);
        int g = (int) (floats[1] * 255);
        int b = (int) (floats[2] * 255);
        int a = (int) (floats[3] * 255);
        return
            ((a & 0xFF) << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8) |
             (b & 0xFF);
    }

    public static float[] decodeFloatColor(double value) {
        long actValue = Double.doubleToRawLongBits(value);
        float red =   ((actValue     ) & 255) / 255F;
        float green = ((actValue >> 8) & 255) / 255F;
        float blue =  ((actValue >> 16) & 255) / 255F;
        float alpha =  ((actValue >> 24) & 255) / 255F;
        long importance =  actValue >> 24L;
        return new float[] {red, green, blue, alpha, importance};
    }

    public static double encodeFloatColor(float r, float g, float b, float a) {
        return encodeFloatColor(r, g, b, a, 1000);
    }

    public static double encodeFloatColor(float r, float g, float b, float a, int importance) {
        return Double.longBitsToDouble(encode(r) | encode(g) << 8 | encode(b) << 16 | encode(a) << 24 | (long) importance << 32);
    }

    private static long encode(float v) {
        return (int) MathHelper.clamp(v * 255, 0, 255);
    }

    public static <T extends GeneticFactoryStorage> Object getCombinerKey(GeneticEntry<T> entry) {
        @SuppressWarnings("unchecked")
        GeneticFactoryStorageType<T> type = (GeneticFactoryStorageType<T>) entry.getStorage().getType();
        return type.combinerKey(entry.getStorage());
    }

    public static List<GeneticEntry<?>> combineAll(List<GeneticEntry<?>> entries) {
        Map<GeneticType<?>, Map<Object, List<GeneticEntry<?>>>> geneticTypeMap = new HashMap<>();
        for (GeneticEntry<?> entry : entries) {
            geneticTypeMap
                .computeIfAbsent(entry.getType(), t -> new HashMap<>())
                .computeIfAbsent(getCombinerKey(entry), k -> new ArrayList<>())
                .add(entry);
        }

        List<GeneticEntry<?>> combined = new ArrayList<>();
        geneticTypeMap.forEach((geneticType, objectListMap) -> {
            for (List<GeneticEntry<?>> toCombine : objectListMap.values()) {
                double value = geneticType.getDataHandler().combineMultipleSources(new DoubleArrayList(toCombine.stream().map(GeneticEntry::getModifier).iterator()));
                combined.add(toCombine.get(0).copy().setModifier(value));
            }
        });
        return combined;
    }
}
