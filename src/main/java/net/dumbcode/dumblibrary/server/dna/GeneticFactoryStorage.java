package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

public interface GeneticFactoryStorage {
    CompoundNBT serialize(CompoundNBT nbt);
    void deserialize(CompoundNBT nbt);

    JsonObject serialize(JsonObject json);
    void deserialize(JsonObject json);

    GeneticFactoryStorageType<?> getType();

    @OnlyIn(Dist.CLIENT)
    void render(MatrixStack stack, GeneticType<?> entry, double value, int x, int y, int width, int height, float ticks);


}
