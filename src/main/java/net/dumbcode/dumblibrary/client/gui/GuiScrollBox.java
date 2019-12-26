package net.dumbcode.dumblibrary.client.gui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
public class GuiScrollBox<T extends GuiScrollboxEntry> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    public static final float SCROLL_AMOUNT = 0.4F;

    private final int width;
    private final int cellHeight;

    private final int cellMax;

    private final int xPos;
    private final int yPos;

    private int insideColor = 0xFF000000;
    private int highlightColor = 0x2299bbff;
    private int cellHighlightColor = 0xFF303030;
    private int borderColor = 0xFFFFFFFF;

    private float scroll;

    private T selectedElement;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Supplier<List<T>> listSupplier;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int lastYClicked = -1;

    public GuiScrollBox(int xPos, int yPos, int width, int cellHeight, int cellMax, Supplier<List<T>> listSupplier) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.cellHeight = cellHeight;
        this.cellMax = Math.max(cellMax, 1);
        this.listSupplier = listSupplier;
    }

    /**
     * Renders the box
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    public void render(int mouseX, int mouseY) {
        this.scroll(0); //This is used to ensure the clamping of the scroll

        List<T> entries = this.listSupplier.get();
        boolean additionalRows = entries.size() > this.cellMax;
        int height = this.getTotalSize(entries.size());
        Rectangle2D.Float scrollBar = this.getScrollBar(entries.size(), height);

        if (additionalRows) {
            this.updateScroll(entries, height, scrollBar.height, mouseY);
        }

        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderUtils.renderSquareStencil(this.xPos, this.yPos, this.xPos + this.width, this.yPos + height, true);

        int borderSize = 1;
        MC.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.enableGUIStandardItemLighting();

        this.renderEntries(entries, height, scrollBar, borderSize, mouseX, mouseY);

        if (additionalRows) {
            this.renderScrollBar(Objects.requireNonNull(scrollBar), borderSize, this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar));
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GlStateManager.disableDepth();
        RenderUtils.renderBorder(this.xPos, this.yPos, this.xPos + this.width, this.yPos + height, borderSize, this.borderColor);
        GlStateManager.enableDepth();
    }

    /**
     * Checks to see if the scroll wheel has been used, and if so then scrolls the screen.
     *
     * @param entries      the list of entries
     * @param totalHeight  the total height of the additional section of the box.
     * @param scrollLength The y size of the scroll bar
     * @param mouseY       the mouse's y position
     */
    private void updateScroll(List<T> entries, int totalHeight, float scrollLength, int mouseY) {
        if (this.lastYClicked != -1) {
            if (!Mouse.isButtonDown(0)) {
                this.lastYClicked = -1;
            } else {
                float oldScroll = this.scroll;
                float pixelsPerEntry = (totalHeight - scrollLength) / (entries.size() - this.cellMax);
                this.scroll((this.lastYClicked - mouseY) / pixelsPerEntry);
                if (oldScroll != this.scroll) {
                    this.lastYClicked = mouseY;
                }
            }
        }
    }

    /**
     * Renders the main entries box
     *
     * @param entries    The list of entries
     * @param height     the total size of this box
     * @param scrollBar  the dimensions of the scroll bar
     * @param borderSize the size of the border
     * @param mouseX     the mouse's x position
     * @param mouseY     the mouse's y position
     */
    private void renderEntries(List<T> entries, int height, Rectangle2D.Float scrollBar, int borderSize, int mouseX, int mouseY) {
        boolean mouseOver = this.isMouseOver(mouseX, mouseY, height);

        for (int i = 0; i < entries.size(); i++) {
            int yStart = (int) (this.yPos + this.cellHeight * i - this.scroll * this.cellHeight);
            //Usually it would be yStart + cellHeight, however because the ystart is offsetted (due to the active selection box), it cancels out
            if (yStart + this.cellHeight >= this.yPos && yStart <= this.yPos + height) {
                Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + this.cellHeight, this.cellHighlightColor);
                Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + borderSize, this.borderColor);
                entries.get(i).draw(this.xPos, yStart, mouseX, mouseY);
            }

            //Draw highlighted section of the cell (if mouse is over)
            if (!this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar) && mouseOver && mouseY >= yStart && mouseY < yStart + this.cellHeight) {
                Gui.drawRect(this.xPos, yStart, this.xPos + this.width, yStart + this.cellHeight, this.highlightColor);
            }
        }
    }

    /**
     * Gets the scrollbar position and dimensions
     *
     * @param height the height of the scroll box
     * @param listSize the size of the entries
     * @return a rectangle of [xPosition, yPosition, xSize, ySize]
     */
    private Rectangle2D.Float getScrollBar(int listSize, int height) {
        int scrollBarWidth = 8;
        int scrollBarLeft = this.xPos + this.width - scrollBarWidth;

        int ySize = (listSize - this.cellMax) * this.cellHeight;
        if(ySize > 0) {
            float scrollLength = MathHelper.clamp(height / ySize, 32, height - 8);
            float scrollYStart = this.scroll * this.cellHeight * (height - scrollLength) / (Math.max((listSize - this.cellMax) * this.cellHeight, 1)) + this.yPos - 1;
            if (scrollYStart < this.yPos - 1) {
                scrollYStart = this.yPos - 1F;
            }
            return new Rectangle2D.Float(scrollBarLeft, scrollYStart, scrollBarWidth, scrollLength);
        }
        return new Rectangle2D.Float(0,0,0,0);
    }

    /**
     * Checks to see if the mouse is over the scrollbar.
     *
     * @param mouseX    the mouse's x
     * @param mouseY    the mouse's y
     * @param height    the height of the box
     * @param scrollBar the dimensions of the scroll bar
     * @return true if the mouse is over the scrollbar, false otherwise.
     */
    private boolean mouseOverScrollBar(int mouseX, int mouseY, int height, Rectangle2D.Float scrollBar) {
        return     mouseX - this.xPos > 0 && mouseX - this.xPos <= this.width
                && mouseY - this.yPos > 0 && mouseY - this.yPos < height

                && mouseX >= scrollBar.x && mouseX <= scrollBar.x + scrollBar.width
                && mouseY >= scrollBar.y && mouseY <= scrollBar.y + scrollBar.height;
    }

    /**
     * Renders the scrollbar
     *
     * @param scrollBar  the dimensions of the scroll bar
     * @param borderSize The size of the border
     * @param mouseOver  whether the mouse is over the scroll-bar or not
     */
    private void renderScrollBar(Rectangle2D.Float scrollBar, int borderSize, boolean mouseOver) {
        Rectangle bar = new Rectangle((int) scrollBar.x, (int) scrollBar.y, (int) scrollBar.width, (int) scrollBar.height);

        //render main bar
        Gui.drawRect(bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight(), this.insideColor);

        //render an overlay to the bar if its overlayed
        if (mouseOver) {
            Gui.drawRect(bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight(), this.highlightColor);
        }

        Gui.drawRect(bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + borderSize, this.borderColor);
        Gui.drawRect(bar.getX(), bar.getY() + bar.getHeight(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight() - borderSize, this.borderColor);
        Gui.drawRect(bar.getX(), bar.getY(), bar.getX() + borderSize, bar.getY() + bar.getHeight(), this.borderColor);
    }

    /**
     * Gets the total size of this box.
     *
     * @param listSize The size of entry list
     * @return the size this element currently.
     */
    private int getTotalSize(int listSize) {
        return Math.min(listSize, this.cellMax) * this.cellHeight;
    }


    /**
     * Gets the total size of this box.
     * @return the size this element currently.
     */
    public int getTotalSize() {
        return Math.min(this.listSupplier.get().size(), this.cellMax) * this.cellHeight;
    }

    /**
     * Called when the mouse is clicked.
     *
     * @param mouseX      the mouse's x position
     * @param mouseY      the mouse's y position
     * @param mouseButton the mouse button clicked.
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            List<T> entries = this.listSupplier.get();

            boolean additionalRows = entries.size() > this.cellMax;
            int height = this.getTotalSize(entries.size());
            Rectangle2D.Float scrollBar = this.getScrollBar(entries.size(), height);

            //Scroll bar clicked
            if (additionalRows && this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar)) {
                this.lastYClicked = mouseY;
                return;
            }

            if (this.isMouseOver(mouseX, mouseY, height) && mouseY - this.yPos < height) {
                this.clickedEntry(entries, mouseX, mouseY);
            }
        }
    }

    /**
     * Tests each entry and checks to see if it were clicked. THIS METHOD ASSUMES THAT THE MOUSE IS DOWN
     *
     * @param entries The list of entries
     * @param mouseX  the mouse's x
     * @param mouseY  the mouse's y
     */
    private void clickedEntry(List<T> entries, int mouseX, int mouseY) {
        for (int i = 0; i < entries.size(); i++) {
            int yStart = (int) (this.yPos + this.cellHeight * i - this.scroll * this.cellHeight);
            if (mouseY - this.yPos <= this.cellHeight * (i + 1) - this.scroll * this.cellHeight) {
                entries.get(i).onClicked(mouseX - this.xPos, mouseY - yStart, mouseX, mouseY);
                this.selectedElement = entries.get(i);
                break;
            }
        }
    }

    /**
     * Handles the mouse input.
     * Should be called from {@link GuiScreen#handleMouseInput()}
     */
    public void handleMouseInput() {
        int mouseInput = Mouse.getEventDWheel();
        if (mouseInput != 0) {
            this.scroll((mouseInput < 0 ? -1 : 1) * SCROLL_AMOUNT);
        }
    }

    /**
     * Checks to see if the mouse is over this element
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     * @return true if the mouse is over this element, false otherwise
     */
    public boolean isMouseOver(int mouseX, int mouseY, int height) {
        return     mouseX - this.xPos > 0
                && mouseX - this.xPos <= this.width

                && mouseY - this.yPos > 0
                && mouseY - this.yPos <= height;
    }


    /**
     * Scrolls the gui by a certain amount
     * @param amount
     */
    public void scroll(float amount) {
        this.scroll -= amount;
        this.scroll = MathHelper.clamp(this.scroll, 0, Math.max(this.listSupplier.get().size() - this.cellMax, 0));
    }
}
