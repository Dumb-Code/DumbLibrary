package net.dumbcode.dumblibrary.client.gui;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Supplier;

public class GuiDropdownBox<T extends SelectListEntry> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private final GuiScrollBox<T> scrollBox;

    @Getter @Setter private boolean open;
    private int cellHeight;
    private int xPos;
    private int yPos;
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

    /**
     * Renders the box
     *
     * @param mouseX the mouse's x position
     * @param mouseY the mouse's y position
     */
    public void render(int mouseX, int mouseY) {
        if (this.open) {
            this.scrollBox.render(mouseX, mouseY);
        }

        this.renderMainCell(mouseX, mouseY);
    }

    /**
     * Checks to see if the mouse is within the x range of this box, and below the top of the topmost cell.
     *
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     * @return true if it is within range, false otherwise
     */
    private boolean mouseOnTopCell(int mouseX, int mouseY) {
        return mouseX - this.xPos > 0 && mouseY - this.yPos > 0 && mouseX - this.xPos <= this.width && mouseY - this.yPos <= this.cellHeight;
    }

    /**
     * Renders the main cell. If the user is searching, then this will render the search term instead of the cell entry.
     *
     * @param mouseX the mouse's x
     * @param mouseY the mouse's y
     */
    private void renderMainCell(int mouseX, int mouseY) {
        //Draw the main background
        Gui.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, this.scrollBox.getBorderColor());
        Gui.drawRect(this.xPos+1, this.yPos+1, this.xPos + this.width-1, this.yPos + this.cellHeight-1, this.scrollBox.getInsideColor());

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderUtils.renderSquareStencil(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, true, 2, GL11.GL_LEQUAL);
        if (!this.search.isEmpty()) {
            MC.fontRenderer.drawString(this.search, this.xPos + 5, this.yPos + this.cellHeight / 2 - MC.fontRenderer.FONT_HEIGHT / 2, -1);
        } else if (this.getActive() != null) {
            this.getActive().draw(this.xPos, this.yPos, mouseX, mouseY);
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);

        //Draw the highlighted section of the main part, if the mouse is over
        if (mouseX - this.xPos > 0 && mouseX - this.xPos <= this.width && mouseY >= this.yPos && mouseY < this.yPos + this.cellHeight) {
            Gui.drawRect(this.xPos, this.yPos, this.xPos + this.width, this.yPos + this.cellHeight, this.scrollBox.getHighlightColor());
        }


    }

    /**
     * Called when the mouse is clicked.
     *
     * @param mouseX      the mouse's x position
     * @param mouseY      the mouse's y position
     * @param mouseButton the mouse button clicked.
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(this.open && this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            SelectListEntry active = this.getActive();
            this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
            if(active != this.getActive()) {
                this.open = !this.open;
            }
            return;
        }
        if (mouseButton == 0) {
            if (this.mouseOnTopCell(mouseX, mouseY)) {
                this.open = !this.open;
                return;
            }
        }
        this.open = false;
        this.search = "";
    }

    /**
     * Handles the mouse input.
     * Should be called from {@link GuiScreen#handleMouseInput()}
     */
    public void handleMouseInput() {
        if(this.open) {
            this.scrollBox.handleMouseInput();
        }
    }

    /**
     * Handles the keyboard input.
     * Should be called from {@link GuiScreen#handleKeyboardInput()}
     */
    @SuppressWarnings("unused")
    public void handleKeyboardInput() {
        if (!this.open) {
            return;
        }
        char c = Keyboard.getEventCharacter();
        if (Keyboard.getEventKey() == 0 && c >= ' ' || Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_BACK) {
                if (!this.search.isEmpty()) {
                    this.search = this.search.substring(0, this.search.length() - 1);
                }
            } else if (ChatAllowedCharacters.isAllowedCharacter(c)) {
                this.search += Character.toLowerCase(c);
                this.scrollBox.setScroll(0);
            }
        }
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