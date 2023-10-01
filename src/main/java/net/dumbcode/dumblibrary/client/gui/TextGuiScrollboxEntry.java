package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

@RequiredArgsConstructor
public class TextGuiScrollboxEntry implements GuiScrollboxEntry {
    private final ITextComponent component;

    @Override
    public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
        Minecraft.getInstance().stack.drawString(font, this.component, x + 2, y + 2, -1);
    }

    @Override
    public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
        return false;
    }
}
