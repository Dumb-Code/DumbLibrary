package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class TitledSimpleSlider extends SimpleSlider {

    private final ITextComponent title;

    public TitledSimpleSlider(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler, ITextComponent title) {
        super(xPos, yPos + 5, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
        this.title = title;
    }

    public TitledSimpleSlider(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler, @Nullable ISlider par, ITextComponent title) {
        super(xPos, yPos + 5, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler, par);
        this.title = title;
    }

    public TitledSimpleSlider(int xPos, int yPos, ITextComponent displayStr, double minVal, double maxVal, double currentVal, IPressable handler, ISlider par, ITextComponent title) {
        super(xPos, yPos + 5, displayStr, minVal, maxVal, currentVal, handler, par);
        this.title = title;
    }

    @Override
    public void renderButton(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        super.renderButton(stack, mouseX, mouseY, ticks);
        FontRenderer font = Minecraft.getInstance().font;
        int width = font.width(this.title);
        int startX = this.x + (this.width - width) / 2;
        stack.drawString(font, this.title, startX, this.y - font.lineHeight - 1, -1);
    }
}
