package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nullable;

public class SimpleSlider extends AbstractSliderButton {

    public SimpleSlider(int xPos, int yPos, int width, int height, Component prefix, Component suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, Button.OnPress handler) {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
    }

    public SimpleSlider(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler, @Nullable ISlider par) {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler, par);
    }

    public SimpleSlider(int xPos, int yPos, ITextComponent displayStr, double minVal, double maxVal, double currentVal, IPressable handler, ISlider par) {
        super(xPos, yPos, displayStr, minVal, maxVal, currentVal, handler, par);
    }

    @Override
    public void renderButton(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        if(!this.visible) {
            return;
        }
        if (this.dragging) {
            this.sliderValue = (mouseX - (this.x + 4)) / (float)(this.width - 8);
            updateSlider();
        }
        Minecraft mc = Minecraft.getInstance();
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        int thumbLeft = this.x + (int)(this.sliderValue * (float)(this.width - 8));
        int thumbTop = this.y + 1;
        int thumbRight = thumbLeft + 8;
        int thumbBottom = thumbTop + this.height - 2;

        StencilStack.pushSquareStencil(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, StencilStack.Type.NOT);
        stack.fill(this.x, this.y + 4, this.x + this.width, this.y + this.height - 4, 0xCF193B59);
        if(this.isHovered && this.active) {
            stack.fill(this.x, this.y + 4, this.x + this.width, this.y + this.height - 4, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, x, y + 4, x + width, y + height - 4, 1, 0xFF577694);
        StencilStack.popStencil();

        stack.fill(thumbLeft, thumbTop, thumbRight, thumbBottom, 0xCF193B59);
        if(this.isHovered && this.active) {
            stack.fill(thumbLeft, thumbTop, thumbRight, thumbBottom, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 1, 0xFF577694);

        ITextComponent buttonText = this.getMessage();
        int strWidth = mc.font.width(buttonText);
        int ellipsisWidth = mc.font.width("...");

        if (strWidth > this.width - 6 && strWidth > ellipsisWidth)
            //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
            buttonText = Component.literal(mc.font.substrByWidth(buttonText, this.width - 6 - ellipsisWidth).getString() + "...");

        stack.drawCenteredString(mc.font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, this.getFGColor());
    }

    public void render(int x, int y, int width, int height, double sliderValue, ITextComponent buttonText, int fgColour, boolean active, GuiGraphics stack, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int thumbLeft = x + (int)(sliderValue * (float)(width - 8));
        int thumbTop = y + 1;
        int thumbRight = thumbLeft + 8;
        int thumbBottom = thumbTop + height - 2;

        StencilStack.pushSquareStencil(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, StencilStack.Type.NOT);
        stack.fill(x, y + 4, x + width, y + height - 4, 0xCF193B59);
        if(isHovered && active) {
            stack.fill(x, y + 4, x + width, y + height - 4, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, x, y + 4, x + width, y + height - 4, 1, 0xFF577694);
        StencilStack.popStencil();

        stack.fill(thumbLeft, thumbTop, thumbRight, thumbBottom, 0xCF193B59);
        if(isHovered && active) {
            stack.fill(thumbLeft, thumbTop, thumbRight, thumbBottom, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 1, 0xFF577694);

        int strWidth = mc.font.width(buttonText);
        int ellipsisWidth = mc.font.width("...");

        if (strWidth > width - 6 && strWidth > ellipsisWidth)
            //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
            buttonText = Component.literal(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

        stack.drawCenteredString(mc.font, buttonText, x + width / 2, y + (height - 8) / 2, fgColour);
    }
}
