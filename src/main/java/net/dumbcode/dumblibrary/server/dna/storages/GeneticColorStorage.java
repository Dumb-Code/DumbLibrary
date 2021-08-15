package net.dumbcode.dumblibrary.server.dna.storages;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GeneticColorStorage extends RandomUUIDStorage<GeneticTint> {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, GeneticType<?, GeneticTint> entry, GeneticTint value, int x, int y, int width, int height, float ticks) {
        int primary = partToColour(value.getPrimary());
        int secondary = partToColour(value.getSecondary());

        AbstractGui.fill(stack, x, y, x + width, y + height - 1, primary);
        AbstractGui.fill(stack, x, (int) (y + height * 0.25F) + 1, x + width, (int) (y + height * 0.75F) - 1, secondary);
    }

    private int partToColour(GeneticTint.Part p) {
        float importanceMod = MathHelper.clamp(p.getImportance() / (float) GeneticUtils.DEFAULT_COLOUR_IMPORTANCE * 2, 0F, 1F);
        return (((int) (p.getA() * 255 * importanceMod) & 0xFF) << 24) |
                (((int) (p.getR() * 255) & 0xFF) << 16) |
                (((int) (p.getG() * 255) & 0xFF) << 8) |
                ((int) (p.getB() * 255) & 0xFF);
    }

}
