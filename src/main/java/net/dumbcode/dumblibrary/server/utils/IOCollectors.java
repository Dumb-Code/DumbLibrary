package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class IOCollectors {

    public static Collector<JsonElement, JsonArray, JsonArray> toJsonArray() { return toTypeJsonArray(JsonArray::add); }
    public static Collector<String, JsonArray, JsonArray> toJsonArrayString() { return toTypeJsonArray(JsonArray::add); }
    public static Collector<Number, JsonArray, JsonArray> toJsonArrayNumber() { return toTypeJsonArray(JsonArray::add); }
    public static Collector<Boolean, JsonArray, JsonArray> toJsonArrayBoolean() { return toTypeJsonArray(JsonArray::add); }
    public static Collector<Character, JsonArray, JsonArray> toJsonArrayCharacter() { return toTypeJsonArray(JsonArray::add); }

    private static <T> Collector<T, JsonArray, JsonArray> toTypeJsonArray(BiConsumer<JsonArray, T> accumulator) {
        return new CollectorImpl<>(
                JsonArray::new,
                accumulator,
                (arr1, arr2) -> { arr1.addAll(arr2); return arr1; }
        );
    }

    public static Collector<NBTBase, NBTTagList, NBTTagList> toNBTTagList() {
        return new CollectorImpl<>(
                NBTTagList::new,
                NBTTagList::appendTag,
                (list1, list2) -> {list2.forEach(list1::appendTag); return list1; }
        );
    }

    @RequiredArgsConstructor
    private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {

        private static final Set<Collector.Characteristics> CHARACTERISTICS = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;

        @Override
        public Supplier<A> supplier() {
            return this.supplier;
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return this.accumulator;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return this.combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return a -> (R) a;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}