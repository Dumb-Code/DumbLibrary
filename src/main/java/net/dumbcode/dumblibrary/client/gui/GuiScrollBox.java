package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class GuiScrollBox<T extends GuiScrollboxEntry> extends Widget {

    private static final Minecraft MC = Minecraft.getInstance();

    public static final float SCROLL_AMOUNT = 0.4F;

    private final int cellHeight;

    private final int cellMax;

    private int insideColor = 0xFF000000;
    private int highlightColor = 0x2299bbff;
    private int cellHighlightColor = 0xFF303030;
    private int cellSelectedColor = 0xFFA0A0A0;
    private int borderColor = 0xFFFFFFFF;

    private float scroll;

    private T selectedElement;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Supplier<List<T>> listSupplier;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private double lastYClicked = -1;

    public GuiScrollBox(int x, int y, int width, int cellHeight, int cellMax, Supplier<List<T>> listSupplier) {
        super(x, y, width, 0, new StringTextComponent(""));
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
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        this.scroll(0); //This is used to ensure the clamping of the scroll

        List<T> entries = this.listSupplier.get();
        boolean additionalRows = entries.size() > this.cellMax;
        int height = this.getTotalSize(entries.size());
        float[] scrollBar = this.getScrollBar(entries.size(), height);

        if (additionalRows) {
            this.updateScroll(entries, height, scrollBar[3], mouseY);
        }

        if (!Minecraft.getInstance().getMainRenderTarget().isStencilEnabled()) {
            Minecraft.getInstance().getMainRenderTarget().enableStencil();
        }

        StencilStack.pushSquareStencil(stack, this.x, this.y, this.x + this.width, this.y + height);

        int borderSize = 1;
        MC.textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        RenderHelper.setupFor3DItems();

        this.renderEntries(stack, entries, height, scrollBar, borderSize, mouseX, mouseY);

        if (additionalRows) {
            this.renderScrollBar(stack, scrollBar, borderSize, this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar));
        }

        RenderHelper.turnOff();
        StencilStack.popStencil();

        RenderUtils.renderBorder(stack, this.x, this.y, this.x + this.width, this.y + height, borderSize, this.borderColor);
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
            float oldScroll = this.scroll;
            float pixelsPerEntry = (totalHeight - scrollLength) / (entries.size() - this.cellMax);
            this.scroll((float) ((this.lastYClicked - mouseY) / pixelsPerEntry));
            if (oldScroll != this.scroll) {
                this.lastYClicked = mouseY;
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
    private void renderEntries(MatrixStack stack, List<T> entries, int height, float[] scrollBar, int borderSize, int mouseX, int mouseY) {
        boolean mouseOver = this.isMouseOver(mouseX, mouseY, height);

        List<T> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(GuiScrollboxEntry::zLevel));

        for (T entry : sorted) {
            int yStart = (int) (this.y + this.cellHeight * entries.indexOf(entry) - this.scroll * this.cellHeight);
            //Usually it would be yStart + cellHeight, however because the ystart is offsetted (due to the active selection box), it cancels out
            if (yStart + this.cellHeight >= this.y && yStart <= this.y + height) {
                AbstractGui.fill(stack, this.x, yStart, this.x + this.width, yStart + this.cellHeight, entry == this.selectedElement ? this.cellSelectedColor : this.cellHighlightColor);
                AbstractGui.fill(stack, this.x, yStart, this.x + this.width, yStart + borderSize, this.borderColor);

                boolean mouseOverElement = !this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar) && mouseOver && mouseY >= yStart && mouseY < yStart + this.cellHeight;

                entry.draw(stack, this.x, yStart, mouseX, mouseY, mouseOverElement);

                //Draw highlighted section of the cell (if mouse is over)
                if (mouseOverElement) {
                    AbstractGui.fill(stack, this.x, yStart, this.x + this.width, yStart + this.cellHeight, this.highlightColor);
                }

                entry.postDraw(this.x, yStart, mouseX, mouseY);
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
    private float[] getScrollBar(int listSize, int height) {
        int scrollBarWidth = 8;
        int scrollBarLeft = this.x + this.width - scrollBarWidth;

        int ySize = (listSize - this.cellMax) * this.cellHeight;
        if(ySize > 0) {
            float scrollLength = MathHelper.clamp(height / ySize, 32, height - 8);
            float scrollYStart = this.scroll * this.cellHeight * (height - scrollLength) / (Math.max((listSize - this.cellMax) * this.cellHeight, 1)) + this.y - 1;
            if (scrollYStart < this.y - 1) {
                scrollYStart = this.y - 1F;
            }
            return new float[] { scrollBarLeft, scrollYStart, scrollBarWidth, scrollLength };
        }
        return new float[4];
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
    private boolean mouseOverScrollBar(double mouseX, double mouseY, int height, float[] scrollBar) {
        return     mouseX - this.x > 0 && mouseX - this.x <= this.width
                && mouseY - this.y > 0 && mouseY - this.y < height

                && mouseX >= scrollBar[0] && mouseX <= scrollBar[0] + scrollBar[2]
                && mouseY >= scrollBar[1] && mouseY <= scrollBar[1] + scrollBar[3];
    }

    /**
     * Renders the scrollbar
     *
     * @param scrollBar  the dimensions of the scroll bar
     * @param borderSize The size of the border
     * @param mouseOver  whether the mouse is over the scroll-bar or not
     */
    private void renderScrollBar(MatrixStack stack, float[] scrollBar, int borderSize, boolean mouseOver) {
        Rectangle2d bar = new Rectangle2d((int) scrollBar[0], (int) scrollBar[1], (int) scrollBar[2], (int) scrollBar[3]);

        //render main bar
        AbstractGui.fill(stack, bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight(), this.insideColor);

        //render an overlay to the bar if its overlayed
        if (mouseOver) {
            AbstractGui.fill(stack, bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight(), this.highlightColor);
        }

        AbstractGui.fill(stack, bar.getX(), bar.getY(), bar.getX() + bar.getWidth(), bar.getY() + borderSize, this.borderColor);
        AbstractGui.fill(stack, bar.getX(), bar.getY() + bar.getHeight(), bar.getX() + bar.getWidth(), bar.getY() + bar.getHeight() - borderSize, this.borderColor);
        AbstractGui.fill(stack, bar.getX(), bar.getY(), bar.getX() + borderSize, bar.getY() + bar.getHeight(), this.borderColor);
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
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        List<T> entries = this.listSupplier.get();

        boolean additionalRows = entries.size() > this.cellMax;
        int height = this.getTotalSize(entries.size());
        float[] scrollBar = this.getScrollBar(entries.size(), height);

        //Scroll bar clicked
        if (additionalRows && this.mouseOverScrollBar(mouseX, mouseY, height, scrollBar)) {
            this.lastYClicked = mouseY;
            return true;
        }

        if (this.isMouseOver(mouseX, mouseY, height) && mouseY - this.y < height) {
            this.clickedEntry(entries, mouseX, mouseY, mouseButton);
            return true;
        }
        return false;
    }

    /**
     * Tests each entry and checks to see if it were clicked. THIS METHOD ASSUMES THAT THE MOUSE IS DOWN
     *
     * @param entries The list of entries
     * @param mouseX  the mouse's x
     * @param mouseY  the mouse's y
     */
    private void clickedEntry(List<T> entries, double mouseX, double mouseY, int mouseButton) {
        for (T entry : entries) {
            if(entry.globalClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        if(mouseButton == 0) {
            for (int i = 0; i < entries.size(); i++) {
                int yStart = (int) (this.y + this.cellHeight * i - this.scroll * this.cellHeight);
                if (mouseY - this.y <= this.cellHeight * (i + 1) - this.scroll * this.cellHeight) {
                    if (entries.get(i).onClicked(mouseX - this.x, mouseY - yStart, mouseX, mouseY)) {
                        this.selectedElement = entries.get(i);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double amount) {
        if (amount != 0) {
            int scroll = amount < 0 ? -1 : 1;
            for (T t : this.listSupplier.get()) {
                if (t.consumeScroll(scroll)) {
                    return true;
                }
            }
            this.scroll(scroll * SCROLL_AMOUNT);
        }
        return false;
    }

    /**
     * Checks to see if the mouse is over this element
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     * @return true if the mouse is over this element, false otherwise
     */
    public boolean isMouseOver(double mouseX, double mouseY, int height) {
        return     mouseX - this.x > 0
                && mouseX - this.x <= this.width

                && mouseY - this.y > 0
                && mouseY - this.y <= height;
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
