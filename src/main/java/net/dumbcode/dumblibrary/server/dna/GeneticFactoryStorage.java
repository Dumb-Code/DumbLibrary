package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

public interface GeneticFactoryStorage<O> {
    CompoundTag serialize(CompoundTag nbt);
    void deserialize(CompoundTag nbt);

    JsonObject serialize(JsonObject json);
    void deserialize(JsonObject json);

    Object getCombinerKey();

    @OnlyIn(Dist.CLIENT)
    void render(GuiGraphics stack, GeneticType<?, O> entry, O value, int x, int y, int width, int height, float ticks);


}
