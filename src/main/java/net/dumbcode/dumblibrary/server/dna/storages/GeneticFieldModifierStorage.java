package net.dumbcode.dumblibrary.server.dna.storages;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Getter
@Setter
@Accessors(chain = true)
public class GeneticFieldModifierStorage extends RandomUUIDStorage<Float> {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics stack, GeneticType<?, Float> type, Float value, int x, int y, int width, int height, float ticks) {
        int color = -1;
        String prefix = "";
        if(value > 0) {
            color = 0xFF00FF00;
            prefix = "+";
        } else if(value < 0) {
            color = 0xFFFF0000;
        }
        RenderUtils.renderScrollingText(stack, type.getTranslationComponent().append(" " + prefix + Math.round(value * 100) + "%"), ticks, x+2, y+4, width-2, color);
    }

    @Override
    public Object getCombinerKey() {
        return null;
    }
}
