package net.dumbcode.dumblibrary.client.gui.filebox;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.gui.GuiConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;

public class GuiFileArea extends GuiButton {

    private final String dropboxTitle;
    private final FilenameFilter filter;

    @Getter
    private File file;
    private FileDropboxFrame dropboxFrame;

    public GuiFileArea(int buttonId, int x, int y, int widthIn, int heightIn, String dropboxTitle, @Nullable FilenameFilter filter) {
        super(buttonId, x, y, widthIn, heightIn, "null");
        this.dropboxTitle = dropboxTitle;
        this.filter = filter;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);

        FontRenderer fontRenderer = mc.fontRenderer;
        if(this.file == null) {
            fontRenderer.drawString("No File Uploaded", this.x + 25, this.y + (this.height - fontRenderer.FONT_HEIGHT)/2 + 1, 0xFF3333);
        } else {
            StringBuilder textToDraw = new StringBuilder();
            for (char c : this.file.getName().toCharArray()) {
                if(fontRenderer.getStringWidth(textToDraw.toString() + c) < this.width - 30) {
                    textToDraw.append(c);
                }
            }
            fontRenderer.drawString(textToDraw.toString(), this.x + 25, this.y + (this.height - fontRenderer.FONT_HEIGHT)/2 + 1, GuiConstants.NICE_WHITE);
        }
//        super.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            if(this.dropboxFrame != null) { //Should be impossible
                this.interrupt();
            }
            this.dropboxFrame = new FileDropboxFrame(this.dropboxTitle, this.filter, f -> this.file = f);
            return true;
        }
        return false;
    }

    public void interrupt() {
        if(this.dropboxFrame != null) {
            this.dropboxFrame.dispose();
            this.dropboxFrame = null;
        }
    }
}
