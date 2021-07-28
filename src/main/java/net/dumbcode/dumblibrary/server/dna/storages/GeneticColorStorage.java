package net.dumbcode.dumblibrary.server.dna.storages;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GeneticColorStorage extends RandomUUIDStorage {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, EntityGeneticRegistry.Entry<?> entry, int x, int y, int height, int width) {
        int colorInt = GeneticUtils.decodeFloatColorInt(entry.getValue());
        AbstractGui.fill(stack, x, y, width, height, 0xFF000000 | colorInt);
    }
}
