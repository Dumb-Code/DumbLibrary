package net.dumbcode.dumblibrary.server.dna.data;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public interface GeneticDataHandler<O> {

    CompoundNBT write(O o, CompoundNBT nbt);
    JsonObject write(O o, JsonObject json);
    void write(O o, PacketBuffer buffer);

    O read(CompoundNBT nbt);
    O read(JsonObject json);
    O read(PacketBuffer buffer);

    O defaultValue();

    O gaussianValue(Random rand);

    O combineChild(O a, O b);

    O combineMultipleSources(List<O> datas);

    O scale(O value, float modifier);

    @OnlyIn(Dist.CLIENT)
    O renderIsolationEdit(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseDown, O current);
}
