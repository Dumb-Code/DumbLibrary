package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SimpleButton extends Button {
    public SimpleButton(int x, int y, int width, int height, Component title, OnPress onPress) {
        super(x, y, width, height, title, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0xCF193B59);
        RenderUtils.renderBorder(stack, this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 1, 0xFF577694);
        if(this.isHovered() && this.active) {
            stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0x2299bbff);
        }
        stack.drawString(Minecraft.getInstance().font, this.getMessage().getString(), this.getX() + this.width / 2F - Minecraft.getInstance().font.width(this.getMessage())/2F, this.getY() + (this.height - 8) / 2F, this.active ? -1 : 0xFF888888, false);
    }
}
