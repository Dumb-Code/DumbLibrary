package net.dumbcode.dumblibrary.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.SharedConstants;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

public class GuiDropdownBox<T extends SelectListEntry> implements IGuiEventListener {

    private static final Minecraft MC = Minecraft.getInstance();

    private final GuiScrollBox<T> scrollBox;

    @Getter @Setter private boolean open;
    private int cellHeight;
    @Getter private int xPos;
    @Getter private int yPos;
    private int width;
    private String search = "";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Supplier<List<T>> listSupplier;

    public GuiDropdownBox(int xPos, int yPos, int width, int cellHeight, int cellMax, Supplier<List<T>> listSupplier) {
        this.scrollBox = new GuiScrollBox<>(xPos, yPos + cellHeight, width, cellHeight, cellMax, this::getSearchedList);
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.cellHeight = cellHeight;
        this.listSupplier = listSupplier;
    }

    public void setActive(T active) {
        this.scrollBox.setSelectedElement(active);
    }

    public T getActive() {
        return this.scrollBox.getSelectedElement();
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
        this.scrollBox.setXPos(xPos);
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
        this.scrollBox.setYPos(yPos + this.cellHeight);
    }

    /**
     * Renders the box
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        if (this.open) {
            this.scrollBox.render(mouseX, mouseY);
        }

        this.renderMainCell(stack, mouseX, mouseY);
    }

    /**
     * Checks to see if the mouse is within the x range of this box, and below the top of the topmost cell.
     *
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     * @return true if it is within range, false otherwise
     */
    private boolean mouseOnTopCell(double mouseX, double mouseY) {
        return mouseX - this.xPos > 0 && mouseY - this.yPos > 0 && mouseX - this.xPos <= this.width && mouseY - this.yPos <= this.cellHeight;
    }

    /**
     * Renders the main cell. If the user is searching, then this will render the search term instead of the cell entry.
     *
     * @param stack the matrix stack
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     */
    private void renderMainCell(MatrixStack stack, int mouseX, int mouseY) {
        //Draw the main background

        AbstractGui.fill(stack, this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, this.scrollBox.getBorderColor());
        AbstractGui.fill(stack, this.xPos+1, this.yPos+1, this.xPos + this.width-1, this.yPos + this.cellHeight-1, this.scrollBox.getInsideColor());

        StencilStack.pushSquareStencil(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight);
        if (!this.search.isEmpty()) {
            MC.font.draw(stack, this.search, this.xPos + 5, this.yPos + this.cellHeight / 2F - MC.font.lineHeight / 2F, -1);
        } else if (this.getActive() != null) {
            this.getActive().draw(this.xPos, this.yPos, mouseX, mouseY, this.mouseOnTopCell(mouseX, mouseY));
        }
        StencilStack.popStencil();

        //Draw the highlighted section of the main part, if the mouse is over
        if (mouseX - this.xPos > 0 && mouseX - this.xPos <= this.width && mouseY >= this.yPos && mouseY < this.yPos + this.cellHeight) {
            AbstractGui.fill(stack, this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, this.scrollBox.getHighlightColor());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(this.open && this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            SelectListEntry active = this.getActive();
            this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
            if(active != this.getActive()) {
                this.open = !this.open;
            }
            return true;
        }
        if (mouseButton == 0) {
            if (this.mouseOnTopCell(mouseX, mouseY)) {
                this.open = !this.open;
                return true;
            }
        }
        this.open = false;
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
    public boolean isMouseOver(int mouseX, int mouseY) {
        return this.mouseOnTopCell(mouseX, mouseY) || (this.open && this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize()));
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