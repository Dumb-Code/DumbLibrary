package net.dumbcode.dumblibrary.client.gui;

import net.minecraft.client.gui.GuiGraphics;

public interface GuiScrollboxEntry {

    /**
     * Draws the element
     *
     * @param stack the matrix stack
     * @param x the elements x position
     * @param y the elements y position
     * @param mouseX the mouse X position
     * @param mouseY the mouse Y position
     * @param mouseOver whether the mouse is over
     */
    default void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
    }


    /**
     * Draws the element
     *
     * @param x the elements x position
     * @param y the elements y position
     * @param mouseX the mouse X position
     * @param mouseY the mouse Y position
     */
    default void postDraw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY) {
    }

    /**
     * Called when the entry is clicked
     *
     * @param relMouseX the relative mouse's x position for this entry
     * @param relMouseY the relative mouse's y position for this entry
     * @param mouseX the actual mouse's x position
     * @param mouseY the actual mouse's y position
     * @return true if this element should be set as the selected, false otherwise
     */
    default boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
        return true;
    }

    default boolean globalClicked(double mouseX, double mouseY, int mouseButton) {
        return false;//return true to cancel propagation
    }

    default boolean consumeScroll(int scrollAmount) {
        return false;
    }

    default int zLevel() {
        return 1;
    }
}
