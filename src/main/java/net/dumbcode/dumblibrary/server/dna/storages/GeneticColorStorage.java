package net.dumbcode.dumblibrary.server.dna.storages;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.dna.DefaultGeneticFactoryStorageTypes;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorageType;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GeneticColorStorage extends RandomUUIDStorage {
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(MatrixStack stack, GeneticType<?> entry, double value, int x, int y, int width, int height, float ticks) {
        int colorInt = GeneticUtils.decodeFloatColorInt(value);
        AbstractGui.fill(stack, x, y, x+width, y+height-1, 0xFF000000 | colorInt);
    }

}
