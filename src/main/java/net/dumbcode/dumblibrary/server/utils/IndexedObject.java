package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.util.JSONUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Value
public class IndexedObject<T> {
    private final T object;
    private final float index;

    public static <T> List<T> sortIndex(List<IndexedObject<T>> indexed) {
        return indexed.stream()
            .sorted(Comparator.comparing(IndexedObject::getIndex))
            .map(IndexedObject::getObject)
            .collect(Collectors.toList());
    }

    public static <T, R> Function<IndexedObject<T>, IndexedObject<R>> mapper(Function<T, R> function) {
        return o -> new IndexedObject<>(function.apply(o.getObject()), o.getIndex());
    }

    public static <T> Collector<IndexedObject<T>, List<IndexedObject<T>>, List<T>> sortedList() {
        return CollectorUtils.listDelegate(IndexedObject::sortIndex);
    }


    public static <T> CompoundTag serializeNBT(IndexedObject<T> object, Function<T, INBT> objectSerializer) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("index", object.index);
        tag.put("object", objectSerializer.apply(object.object));
        return tag;
    }

    public static <T> IndexedObject<T> deserializeNBT(CompoundTag tag, Function<INBT, T> deserailizer) {
        return new IndexedObject<>(
            deserailizer.apply(tag.get("object")),
            tag.getFloat("index")
        );
    }

    public static <T> JsonObject serializeJson(IndexedObject<T> object, Function<T, JsonElement> objectSerializer) {
        JsonObject json = new JsonObject();
        json.add("object", objectSerializer.apply(object.object));
        json.addProperty("index", object.index);
        return json;
    }

    public static <T> IndexedObject<T> deserializeJson(JsonObject json, Function<JsonElement, T> deserailizer) {
        return new IndexedObject<>(
            deserailizer.apply(json.get("object")),
            JSONUtils.getAsFloat(json, "index")
        );
    }

    public static <T> void serializeByteBuf(ByteBuf buf, IndexedObject<T> object, Consumer<T> serializer) {
        serializer.accept(object.object);
        buf.writeFloat(object.index);
    }

    public static <T> IndexedObject<T> deserializeByteBuf(ByteBuf buf, Supplier<T> deserializer) {
        return new IndexedObject<>(
            deserializer.get(),
            buf.readFloat()
        );
    }
}
