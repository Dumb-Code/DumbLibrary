package net.dumbcode.dumblibrary.server.dna.data;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.SimpleSlider;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public enum FloatGeneticDataHandler implements GeneticDataHandler<Float> {
    INSTANCE;

    @Override
    public Float gaussianValue(Random rand) {
        return (float) rand.nextGaussian();
    }

    @Override
    public Float defaultValue() {
        return 0F;
    }

    @Override
    public CompoundNBT write(Float o, CompoundNBT nbt) {
        nbt.putFloat("value", o);
        return nbt;
    }

    @Override
    public JsonObject write(Float o, JsonObject json) {
        json.addProperty("value", o);
        return json;
    }

    @Override
    public void write(Float o, PacketBuffer buffer) {
        buffer.writeFloat(o);
    }

    @Override
    public Float read(CompoundNBT nbt) {
        return nbt.getFloat("value");
    }

    @Override
    public Float read(JsonObject json) {
        return JSONUtils.getAsFloat(json, "value", 0);
    }

    @Override
    public Float read(PacketBuffer buffer) {
        return buffer.readFloat();
    }

    @Override
    public Float combineChild(Float a, Float b) {
        return (a + b) / 2;
    }

    @Override
    public Float combineMultipleSources(List<Float> floats) {
        float sum = 0;
        for (Float f : floats) {
            sum += f;
        }
        return sum;
    }

    @Override
    public Float scale(Float value, float modifier) {
        return value * modifier;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public Widget createIsolationWidget(int x, int y, int width, int height, boolean isSecondary, Float current, Consumer<Float> setter) {
        return new SimpleSlider(x, y, width, height, new StringTextComponent(""), new StringTextComponent("%"), 0, 100, current * 100, false, true, p -> {}, s -> setter.accept((float) s.sliderValue));
    }
}
