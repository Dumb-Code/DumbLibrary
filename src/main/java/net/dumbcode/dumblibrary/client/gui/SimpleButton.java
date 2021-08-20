package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class SimpleButton extends Button {
    public SimpleButton(int x, int y, int width, int height, ITextComponent title, IPressable onPress) {
        super(x, y, width, height, title, onPress);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        fill(stack, this.x, this.y, this.x+this.width, this.y+this.height, 0xCF193B59);
        RenderUtils.renderBorder(stack, this.x, this.y, this.x+this.width, this.y+this.height, 1, 0xFF577694);
        if(this.isHovered() && this.active) {
            fill(stack, this.x, this.y, this.x+this.width, this.y+this.height, 0x2299bbff);
        }
        Minecraft.getInstance().font.draw(stack, this.getMessage(), this.x + this.width / 2F - Minecraft.getInstance().font.width(this.getMessage())/2F, this.y + (this.height - 8) / 2F, this.active ? -1 : 0xFF888888);
    }
}
