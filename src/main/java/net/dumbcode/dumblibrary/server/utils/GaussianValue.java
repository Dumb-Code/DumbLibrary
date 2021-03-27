package net.dumbcode.dumblibrary.server.utils;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;

import java.util.Random;

@AllArgsConstructor
public class GaussianValue {
    private final float mean;
    private final float deviation;

    public float getRandomValue(Random random) {
        return (float) (this.mean + random.nextGaussian() * this.deviation);
    }

    public static CompoundNBT writeToNBT(GaussianValue value) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("mean", value.mean);
        nbt.putFloat("deviation", value.deviation);
        return nbt;
    }

    public static GaussianValue readFromNBT(CompoundNBT nbt) {
        return new GaussianValue(nbt.getFloat("mean"), nbt.getFloat("deviation"));
    }

    public static JsonObject writeToJson(GaussianValue value) {
        JsonObject json = new JsonObject();
        json.addProperty("mean", value.mean);
        json.addProperty("deviation", value.deviation);
        return json;
    }

    public static GaussianValue readFromJson(JsonObject json) {
        return new GaussianValue(JSONUtils.getAsFloat(json, "mean"), JSONUtils.getAsFloat(json, "deviation"));
    }

    public static void writeToBuf(GaussianValue value, ByteBuf buf) {
        buf.writeFloat(value.mean);
        buf.writeFloat(value.deviation);
    }

    public static GaussianValue readFromBuf(ByteBuf buf) {
        return new GaussianValue(buf.readFloat(), buf.readFloat());
    }
}
