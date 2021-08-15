package net.dumbcode.dumblibrary.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nullable;

public class SimpleSlider {

    public static void renderSlider(Slider s, MatrixStack stack, int mouseX, int mouseY) {
        render(s.x, s.y, s.getWidth(), s.getHeight(), s.sliderValue, s.getMessage(), s.getFGColor(), s.active, stack, mouseX, mouseY);
    }

    public static void render(int x, int y, int width, int height, double sliderValue, ITextComponent buttonText, int fgColour, boolean active, MatrixStack stack, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        int thumbLeft = x + (int)(sliderValue * (float)(width - 8));
        int thumbTop = y + 1;
        int thumbRight = thumbLeft + 8;
        int thumbBottom = thumbTop + height - 2;

        StencilStack.pushSquareStencil(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, StencilStack.Type.NOT);
        AbstractGui.fill(stack, x, y + 4, x + width, y + height - 4, 0xCF193B59);
        if(isHovered && active) {
            AbstractGui.fill(stack, x, y + 4, x + width, y + height - 4, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, x, y + 4, x + width, y + height - 4, 1, 0xFF577694);
        StencilStack.popStencil();

        AbstractGui.fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0xCF193B59);
        if(isHovered && active) {
            AbstractGui.fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0x2299bbff);
        }
        RenderUtils.renderBorderExclusive(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 1, 0xFF577694);

        int strWidth = mc.font.width(buttonText);
        int ellipsisWidth = mc.font.width("...");

        if (strWidth > width - 6 && strWidth > ellipsisWidth)
            //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
            buttonText = new StringTextComponent(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

        AbstractGui.drawCenteredString(stack, mc.font, buttonText, x + width / 2, y + (height - 8) / 2, fgColour);
    }
}
