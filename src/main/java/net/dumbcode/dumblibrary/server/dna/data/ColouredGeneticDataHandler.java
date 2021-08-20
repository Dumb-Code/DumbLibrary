package net.dumbcode.dumblibrary.server.dna.data;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.gui.ColourWheelSelector;
import net.dumbcode.dumblibrary.client.gui.ImportanceColourPicker;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum ColouredGeneticDataHandler implements GeneticDataHandler<GeneticTint> {
    INSTANCE;

    public static CompoundNBT writePartNBT(GeneticTint.Part part) {
        CompoundNBT c = new CompoundNBT();
        c.putFloat("red", part.getR());
        c.putFloat("green", part.getG());
        c.putFloat("blue", part.getB());
        c.putFloat("alpha", part.getA());
        c.putInt("importance", part.getImportance());
        return c;
    }
    @Override
    public CompoundNBT write(GeneticTint o, CompoundNBT nbt) {
        nbt.put("primary", writePartNBT(o.getPrimary()));
        nbt.put("secondary", writePartNBT(o.getSecondary()));
        return nbt;
    }

    public static JsonObject writePartJson(GeneticTint.Part part) {
        JsonObject c = new JsonObject();
        c.addProperty("red", part.getR());
        c.addProperty("green", part.getG());
        c.addProperty("blue", part.getB());
        c.addProperty("alpha", part.getA());
        c.addProperty("importance", part.getImportance());
        return c;
    }
    @Override
    public JsonObject write(GeneticTint o, JsonObject json) {
        json.add("primary", writePartJson(o.getPrimary()));
        json.add("secondary", writePartJson(o.getSecondary()));
        return json;
    }

    public static void writePartBuffer(GeneticTint.Part part, PacketBuffer buffer) {
        buffer.writeFloat(part.getR());
        buffer.writeFloat(part.getG());
        buffer.writeFloat(part.getB());
        buffer.writeFloat(part.getA());
        buffer.writeInt(part.getImportance());
    }
    @Override
    public void write(GeneticTint o, PacketBuffer buffer) {
        writePartBuffer(o.getPrimary(), buffer);
        writePartBuffer(o.getSecondary(), buffer);
    }

    public static GeneticTint.Part readPartNBT(CompoundNBT part) {
        return new GeneticTint.Part(
            part.getFloat("red"),
            part.getFloat("green"),
            part.getFloat("blue"),
            part.getFloat("alpha"),
            part.getInt("importance")
        );
    }
    @Override
    public GeneticTint read(CompoundNBT nbt) {
        return new GeneticTint(
            readPartNBT(nbt.getCompound("primary")),
            readPartNBT(nbt.getCompound("secondary"))
        );
    }

    public static GeneticTint.Part readPartJson(JsonObject json) {
        return new GeneticTint.Part(
            JSONUtils.getAsFloat(json, "red"),
            JSONUtils.getAsFloat(json, "green"),
            JSONUtils.getAsFloat(json, "blue"),
            JSONUtils.getAsFloat(json, "alpha"),
            JSONUtils.getAsInt(json, "importance")
        );
    }
    @Override
    public GeneticTint read(JsonObject json) {
        return new GeneticTint(
            readPartJson(JSONUtils.getAsJsonObject(json, "primary")),
            readPartJson(JSONUtils.getAsJsonObject(json, "secondary"))
        );
    }

    public static GeneticTint.Part readPartBuffer(PacketBuffer buffer) {
        return new GeneticTint.Part(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }
    @Override
    public GeneticTint read(PacketBuffer buffer) {
        return new GeneticTint(
            readPartBuffer(buffer),
            readPartBuffer(buffer)
        );
    }

    @Override
    public GeneticTint defaultValue() {
        return new GeneticTint(new GeneticTint.Part(1F, 1F, 1F, 1F, 0), new GeneticTint.Part(1F, 1F, 1F, 1F, 0));
    }

    @Override
    public GeneticTint gaussianValue(Random rand) {
        throw new IllegalArgumentException("Should not be called");
    }

    private static GeneticTint.Part combine(GeneticTint.Part a, GeneticTint.Part b) {
        return new GeneticTint.Part(
            (a.getR() + b.getR()) / 2F,
            (a.getG() + b.getG()) / 2F,
            (a.getB() + b.getB()) / 2F,
            (a.getA() + b.getA()) / 2F,
            GeneticUtils.DEFAULT_COLOUR_IMPORTANCE
        );
    }

    @Override
    public GeneticTint combineChild(GeneticTint a, GeneticTint b) {
        return new GeneticTint(
            combine(a.getPrimary(), b.getPrimary()),
            combine(a.getSecondary(), b.getSecondary())
        );
    }

    public static GeneticTint.Part combineMultipleParts(List<GeneticTint.Part> data) {
        float[] total = new float[4];
        int size = 0;
        for (GeneticTint.Part datum : data) {
            int importance = datum.getImportance();

            total[0] += datum.getR() * importance;
            total[1] += datum.getG() * importance;
            total[2] += datum.getB() * importance;
            total[3] += datum.getA() * importance;

            size += importance;
        }

        if(size == 0) {
            size = 1;
            total = new float[] { 1, 1, 1, 1 };
        }

        return new GeneticTint.Part(
            total[0] / size, total[1] / size, total[2] /size, total[3] /size,
            size
        );
    }


    @Override
    public GeneticTint combineMultipleSources(List<GeneticTint> data) {
        return new GeneticTint(
            combineMultipleParts(
                data.stream()
                    .map(GeneticTint::getPrimary)
                    .collect(Collectors.toList())
            ),
            combineMultipleParts(
                data.stream()
                    .map(GeneticTint::getSecondary)
                    .collect(Collectors.toList())
            )
        );
    }

    @Override
    public GeneticTint scale(GeneticTint value, float modifier) {
        return new GeneticTint(
            value.getPrimary().withImportance((int) (value.getPrimary().getImportance() * modifier)),
            value.getSecondary().withImportance((int) (value.getSecondary().getImportance() * modifier))
        );
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public GeneticTint renderIsolationEdit(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseDown, GeneticTint current) {
//        AbstractGui.fill(stack, x, y, x+width, y+height, 0xAABBCCDD);
//        return current;
//    }


    @Override
    public Widget createIsolationWidget(int x, int y, int width, int height, boolean isSecondary, Supplier<GeneticTint> current, Consumer<GeneticTint> setter, GeneticType<?, GeneticTint> type) {
        Supplier<GeneticTint.Part> part = isSecondary ?
            () -> current.get().getSecondary() :
            () -> current.get().getPrimary();
        Consumer<GeneticTint.Part> partConsumer =
            isSecondary ?
                p -> setter.accept(new GeneticTint(current.get().getPrimary(), p)) :
                p -> setter.accept(new GeneticTint(p, current.get().getSecondary()));

        int radii;
        int startX;
        int startY;

        //Tall Space
        if(width < height) {
            radii = width;
            startX = x;
            startY = y + (height - radii) / 2;
        } else { //Wide object
            radii = height;
            startX = x + (width - radii) / 2;
            startY = y;
        }

        GeneticTint.Part now = part.get();
//        return new ColourWheelSelector(startX, startY, radii, (selector, r, g, b) -> partConsumer.accept(new GeneticTint.Part(r, g, b, 1F, part.get().getImportance())))
//            .setColour((int) (now.getR() * 255F), (int) (now.getG() * 255F), (int) (now.getB() * 255F));
        return new ImportanceColourPicker(startX, startY, radii, now, partConsumer);
    }
}
