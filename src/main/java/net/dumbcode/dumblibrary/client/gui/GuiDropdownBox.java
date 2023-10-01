package net.dumbcode.dumblibrary.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

public class GuiDropdownBox<T extends SelectListEntry> extends Widget {

    private static final Minecraft MC = Minecraft.getInstance();

    private final GuiScrollBox<T> scrollBox;

    @Getter @Setter private boolean open;
    private int cellHeight;
    private String search = "";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Supplier<List<T>> listSupplier;


    public GuiDropdownBox(int xPos, int yPos, int width, int cellHeight, int cellMax, Supplier<List<T>> listSupplier) {
        super(xPos, yPos, width, cellHeight, Component.literal(""));
        this.scrollBox = new GuiScrollBox<>(this.x, this.y + cellHeight, width, cellHeight, cellMax, this::getSearchedList);
        this.cellHeight = cellHeight;
        this.listSupplier = listSupplier;
    }

    public void setActive(T active) {
        this.scrollBox.setSelectedElement(active);
    }

    public T getActive() {
        return this.scrollBox.getSelectedElement();
    }

    public void setX(int xPos) {
        this.x = xPos;
        this.scrollBox.x = xPos;
    }

    public void setY(int yPos) {
        this.y = yPos;
        this.scrollBox.y = yPos + this.cellHeight;
    }

    /**
     * Renders the box
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        if (this.open) {
            this.scrollBox.render(stack, mouseX, mouseY, ticks);
        }

        this.renderMainCell(stack, mouseX, mouseY);
    }

    /**
     * Renders the main cell. If the user is searching, then this will render the search term instead of the cell entry.
     *
     * @param stack the matrix stack
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     */
    private void renderMainCell(GuiGraphics stack, int mouseX, int mouseY) {
        //Draw the main background

        AbstractGui.stack.fill(this.x, this.y, this.x + this.width, this.y + this.cellHeight, this.scrollBox.getBorderColor());
        AbstractGui.stack.fill(this.x +1, this.y +1, this.x + this.width-1, this.y + this.cellHeight-1, this.scrollBox.getInsideColor());

        StencilStack.pushSquareStencil(stack, this.x, this.y, this.x + this.width, this.y + this.cellHeight);
        if (!this.search.isEmpty()) {
            MC.stack.drawString(font, this.search, this.x + 5, this.y + this.cellHeight / 2F - MC.font.lineHeight / 2F, -1);
        } else if (this.getActive() != null) {
            this.getActive().draw(stack, this.x, this.y, this.width, this.cellHeight, mouseX, mouseY, this.isMouseOver(mouseX, mouseY));
        }
        StencilStack.popStencil();

        //Draw the highlighted section of the main part, if the mouse is over
        if (mouseX - this.x > 0 && mouseX - this.x <= this.width && mouseY >= this.y && mouseY < this.y + this.cellHeight) {
            AbstractGui.stack.fill(this.x, this.y, this.x + this.width, this.y + this.cellHeight, this.scrollBox.getHighlightColor());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(this.open && this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            SelectListEntry active = this.getActive();
            this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
            if(active != this.getActive()) {
                this.open = !this.open;
                this.scrollBox.setHeight(this.open ? this.scrollBox.getTotalSize() : 0);
            }
            return true;
        }
        if (mouseButton == 0) {
            if (this.isMouseOver(mouseX, mouseY)) {
                this.open = !this.open;
                this.scrollBox.setHeight(this.open ? this.scrollBox.getTotalSize() : 0);
                return true;
            }
        }
        this.open = false;
        this.scrollBox.setHeight(0);
        this.search = "";
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double a) {
        return this.scrollBox.mouseScrolled(x, y, a);
    }

    @Override
    public boolean charTyped(char c, int code) {
        if (!this.open) {
            return false;
        }
        if (code == GLFW.GLFW_KEY_BACKSPACE) {
            if (!this.search.isEmpty()) {
                this.search = this.search.substring(0, this.search.length() - 1);
            }
        } else if (SharedConstants.isAllowedChatCharacter(c)) {
            this.search += Character.toLowerCase(c);
            this.scrollBox.setScroll(0);
        }
        return true;
    }

    /**
     * Checks to see if the mouse is over this element
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     * @return true if the mouse is over this element, false otherwise
     */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || (this.open && this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize()));
    }

    /**
     * Gets the searched list. If {@link #search} is empty, then the main list will be returned. <br>
     * If it is not empty then the main list is searched with each element of the list checking to see if the
     * {@link SelectListEntry#getSearch()} contains the {@link #search} term
     *
     * @return
     */
    private List<T> getSearchedList() {
        if (this.search.isEmpty()) {
            return this.listSupplier.get();
        }
        List<T> list = Lists.newArrayList();
        for (T listEntry : this.listSupplier.get()) {
            if (listEntry.getSearch().toLowerCase().contains(this.search)) {
                list.add(listEntry);
            }
        }
        return list;
    }

}