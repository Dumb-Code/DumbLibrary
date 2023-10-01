package net.dumbcode.dumblibrary.server.dna.data;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.GuiGraphics;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface GeneticDataHandler<O> {

    CompoundNBT write(O o, CompoundNBT nbt);
    JsonObject write(O o, JsonObject json);
    void write(O o, PacketBuffer buffer);

    O read(CompoundNBT nbt);
    O read(JsonObject json);
    O read(PacketBuffer buffer);

    O defaultValue();

    O gaussianValue(Random rand);

    O mutateValue(O value, Random random, float amount);

    O combineChild(O a, O b);

    O combineMultipleSources(List<O> datas);

    O scale(O value, float modifier);

    IFormattableTextComponent getValue(O data);


//    O renderIsolationEdit(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseDown, O current);

    @OnlyIn(Dist.CLIENT)
    AbstractWidget createIsolationWidget(int x, int y, int width, int height, int data, Supplier<O> current, Consumer<O> setter, GeneticType<?, O> type);


}
