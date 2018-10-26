package net.dumbcode.dumblibrary.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class GuiDropdownBox {

    private static Minecraft mc = Minecraft.getMinecraft();

    private static final float SCROLL_AMOUNT = 0.4F;

    private final int width;
    private final int cellHeight;

    private final int cellMax;

    private final int xPos;
    private final int yPos;

    private boolean open;
    private float scroll;

    private String search = "";

    private SelectListEntry active;

    private final Supplier<List<? extends SelectListEntry>> listSupplier;

    private int lastYClicked = -1;

    public GuiDropdownBox(int xPos, int yPos, int width, int cellHeight, int cellMax, Supplier<List<? extends SelectListEntry>> listSupplier) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.cellHeight = cellHeight;
        this.cellMax = Math.max(cellMax, 1);
        this.listSupplier = listSupplier;
    }

    public void render(int mouseX, int mouseY) {
        List<? extends SelectListEntry> entries = this.getSearchedList();

        int height = this.cellHeight + (this.open ?  Math.min(entries.size(), this.cellMax) * this.cellHeight : 0);
        int totalHeight = height - this.cellHeight;

        float scrollLength = -1;
        float scrollYStart = -1;
        int scrollBarWidth = 8;
        int scrollBarLeft = this.xPos + this.width - scrollBarWidth;

        if(entries.size() > this.cellMax) {
            int ySize = (entries.size() - this.cellMax) * this.cellHeight;
            scrollLength = MathHelper.clamp(totalHeight / ySize, 32, totalHeight - 8);
            scrollYStart = this.scroll * this.cellHeight * (totalHeight - scrollLength) / (Math.max((entries.size() -  this.cellMax) * this.cellHeight, 0)) + this.yPos + this.cellHeight - 1;
            if (scrollYStart < this.yPos - 1) {
                scrollYStart = this.yPos - 1;
            }
        }

        if(this.lastYClicked != -1 && entries.size() > this.cellMax) {
            if(!Mouse.isButtonDown(0)) {
                this.lastYClicked = -1;
            } else {
                float oldScroll = this.scroll;
                float pixelsPerEntry = (totalHeight - scrollLength) / (entries.size() - this.cellMax);
                this.scroll((this.lastYClicked - mouseY) / pixelsPerEntry);
                if(oldScroll != this.scroll) {
                    this.lastYClicked = mouseY;
                }
            }
        }
//        int listedCells = Math.min(entries.size(), this.cellMax);

        if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        Gui.drawRect(this.xPos, this.yPos + this.cellHeight, this.xPos + this.width, this.yPos + height, -1);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0x00);

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);

        int relX = mouseX - this.xPos;
        int relY = mouseY - this.yPos;

        int borderSize = 1;
        int borderColor = -1;
        int insideColor = 0xFF000000;
        int insideSelectionColor = 0xFF303030;
        int highlightColor = 0x2299bbff;
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.enableGUIStandardItemLighting();

        if(this.open) {
            for (int i = 0; i < entries.size(); i++) {
                int yStart = (int) (this.yPos + this.cellHeight * (i + 1) - this.scroll * this.cellHeight) - 1;
                //Usually it would be yStart + cellHeight, however because the ystart is offsetted (due to the active selection box), it cancels out
                if(yStart >= this.yPos && yStart <= this.yPos + height) {
                    Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + this.cellHeight, insideSelectionColor);
                    Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + borderSize, borderColor);
                    entries.get(i).draw(this.xPos, yStart);
                }
            }
        }

        boolean highlighedScrollbar = this.lastYClicked != -1;

        if(relX > 0 && relY > 0) {
            if(relX <= this.width){
                if (relY <= this.cellHeight) {
                    Gui.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, highlightColor);
                } else if(relY < height && this.open) {
                    if(entries.size() > this.cellMax && mouseX >= scrollBarLeft && mouseX <= scrollBarLeft + scrollBarWidth && mouseY >= scrollYStart && mouseY <= scrollYStart + scrollLength) {
                        highlighedScrollbar = true;
                    } else if(this.lastYClicked == -1) {
                        for (int i = 0; i < entries.size(); i++) {
                            if(relY <= this.cellHeight * (i + 2) - this.scroll * this.cellHeight) {
                                int yStart = (int) (this.yPos + this.cellHeight * (i + 1) - this.scroll * this.cellHeight);
                                Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + this.cellHeight, highlightColor);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if(this.open) {
            if(entries.size() > this.cellMax) {
                int l = (int) scrollLength;
                int ys = (int) scrollYStart;

                Gui.drawRect(scrollBarLeft, ys, scrollBarLeft + scrollBarWidth, ys + l, insideColor);

                if(highlighedScrollbar) {
                    Gui.drawRect(scrollBarLeft, ys, scrollBarLeft + scrollBarWidth, ys + l, highlightColor);
                }

                Gui.drawRect(scrollBarLeft, ys, scrollBarLeft + scrollBarWidth, ys + borderSize, borderColor);
                Gui.drawRect(scrollBarLeft, ys + l, scrollBarLeft + scrollBarWidth, ys + l - borderSize, borderColor);
                Gui.drawRect(scrollBarLeft, ys, scrollBarLeft + borderSize, ys + l, borderColor);
//                Gui.drawRect(left + w, ys, left + w - borderSize, ys + l, borderColor);
            }
        }

        RenderHelper.disableStandardItemLighting();

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GlStateManager.disableDepth();
        Gui.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, insideColor);


        if(!this.search.isEmpty()) {
            mc.fontRenderer.drawString(this.search, this.xPos + 5, this.yPos + this.cellHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, -1);
        } else if(this.active != null) {
            this.active.draw(this.xPos, this.yPos);
        }

        Gui.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + borderSize, borderColor);
        Gui.drawRect(this.xPos, this.yPos + height, this.xPos + this.width, this.yPos + height - borderSize, borderColor);
        Gui.drawRect(this.xPos, this.yPos + cellHeight, this.xPos + this.width, this.yPos + cellHeight - borderSize, borderColor);
        Gui.drawRect(this.xPos, this.yPos, this.xPos + borderSize, this.yPos + height, borderColor);
        Gui.drawRect(this.xPos + this.width, this.yPos, this.xPos + this.width - borderSize, this.yPos + height, borderColor);
        GlStateManager.enableDepth();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(mouseButton == 0) {
            List<? extends SelectListEntry> entries = this.getSearchedList();

            int height = this.cellHeight + (this.open ?  Math.min(entries.size(), this.cellMax) * this.cellHeight : 0);
            int totalHeight = height - this.cellHeight;

            float scrollLength = -1;
            float scrollYStart = -1;
            int scrollBarWidth = 8;
            int scrollBarLeft = this.xPos + this.width - scrollBarWidth;

            if(entries.size() > this.cellMax) {
                int ySize = (entries.size() - this.cellMax) * this.cellHeight;
                scrollLength = MathHelper.clamp(totalHeight / ySize, 32, totalHeight - 8);
                scrollYStart = this.scroll * this.cellHeight * (totalHeight - scrollLength) / (Math.max((entries.size() -  this.cellMax) * this.cellHeight, 0)) + this.yPos + this.cellHeight - 1;
                if (scrollYStart < this.yPos - 1) {
                    scrollYStart = this.yPos - 1;
                }
            }

            if(this.open && entries.size() > this.cellMax && mouseX >= scrollBarLeft && mouseX <= scrollBarLeft + scrollBarWidth && mouseY >= scrollYStart && mouseY <= scrollYStart + scrollLength) {
                this.lastYClicked = mouseY;
                return;
            }

            int relX = mouseX - this.xPos;
            int relY = mouseY - this.yPos;
            if(relX > 0 && relY > 0) {
                if(relX <= this.width) {
                    if(relY <= this.cellHeight) {
                        this.open = !this.open;
                        return;
                    } else if(relY < height && this.open){
                        for (int i = 0; i < entries.size(); i++) {
                            if(relY <= this.cellHeight * (i + 2) - this.scroll * this.cellHeight) {
                                entries.get(i).onClicked(relX, relY);
                                this.active = entries.get(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
        this.open = false;
        this.search = "";
    }

    public void handleMouseInput() {
        int mouseInput = Mouse.getEventDWheel();
        if(mouseInput != 0) {
            this.scroll((mouseInput < 0 ? -1 : 1) * SCROLL_AMOUNT);
        }
    }

    public void handleKeyboardInput() {
        if(!this.open) {
            return;
        }
        char c = Keyboard.getEventCharacter();
        if (Keyboard.getEventKey() == 0 && c >= ' ' || Keyboard.getEventKeyState()) {
            if(Keyboard.getEventKey() == Keyboard.KEY_BACK) {
                if(!this.search.isEmpty()) {
                    this.search = this.search.substring(0, this.search.length() - 1);
                }
            } else if(ChatAllowedCharacters.isAllowedCharacter(c)) {
                this.search += Character.toLowerCase(c);
                this.scroll = 0;
            }
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        int relX = mouseX - this.xPos;
        int relY = mouseY - this.yPos;
        if(relX > 0 && relY > 0) {
            if(relX <= this.width) {
                if(relY <= this.cellHeight) {
                    return true;
                } else if(this.open){
                    return relY <= this.cellHeight * (Math.min(this.getSearchedList().size(), this.cellMax) + 1);
                }
            }
        }
        return false;
    }

    private List<? extends SelectListEntry> getSearchedList() {
        if(this.search.isEmpty()) {
            return this.listSupplier.get();
        }
        List<SelectListEntry> list = Lists.newArrayList();
        for (SelectListEntry listEntry : this.listSupplier.get()) {
            if(listEntry.getSearch().toLowerCase().contains(this.search)) {
                list.add(listEntry);
            }
        }
        return list;
    }

    public void scroll(float amount) {
        this.scroll -= amount;
        this.scroll = MathHelper.clamp(this.scroll, 0, Math.max(this.getSearchedList().size() -  this.cellMax, 0));
    }

    public SelectListEntry getActive() {
        return this.active;
    }

    public void setActive(SelectListEntry active) {
        this.active = active;
    }

    interface SelectListEntry {
        void draw(int x, int y);

        String getSearch();

        default void onClicked(int relMouseX, int relMouseY) {

        }
    }
}