package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

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

    public static <T> Collector<T, NBTTagList, NBTTagList> toNBTList(Function<T, ? extends NBTBase> func) {
        return new CollectorImpl<>(
            NBTTagList::new,
            (nbtBases, s) -> nbtBases.appendTag(func.apply(s)),
            (list1, list2) -> {list2.forEach(list1::appendTag); return list1; }
        );
    }

    public static <T> Collector<T, List<T>, Stream<T>> shuffler(Random rand) {
        return new CollectorImpl<T, List<T>, Stream<T>>(
            ArrayList::new,
            List::add,
            (list1, list2) -> { list1.addAll(list2); return list1; }
        ).finisher(ts -> { Collections.shuffle(ts, rand); return ts.stream();});
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    @RequiredArgsConstructor
    private static class CollectorImpl<T, A, R> implements Collector<T, A, R> {

        private static final Set<Collector.Characteristics> CHARACTERISTICS = new HashSet<>();

        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;

        private Function<A, R> finisher = a -> (R) a;

        @Override
        public Set<Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}
