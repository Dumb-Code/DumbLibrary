package net.dumbcode.dumblibrary.server.dna;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface GeneticFactoryStorage {
    CompoundNBT serialize(CompoundNBT nbt);
    void deserialize(CompoundNBT nbt);

    JsonObject serialize(JsonObject json);
    void deserialize(JsonObject json);

    @OnlyIn(Dist.CLIENT)
    void render(MatrixStack stack, EntityGeneticRegistry.Entry<?> entry, int x, int y, int height, int width);
}
