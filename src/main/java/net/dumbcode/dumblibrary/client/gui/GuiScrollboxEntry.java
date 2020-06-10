package net.dumbcode.dumblibrary.client.gui;

public interface GuiScrollboxEntry {
    /**
     * Draws the element
     *
     * @param x the elements x position
     * @param y the elements y position
     * @param mouseX the mouse X position
     * @param mouseY the mouse Y position
     */
    @Deprecated
    default void draw(int x, int y, int mouseX, int mouseY) {

    }

    default void draw(int x, int y, int mouseX, int mouseY, boolean mouseOver) {
        draw(x, y, mouseX, mouseY);
    }


    /**
     * Draws the element
     *
     * @param x the elements x position
     * @param y the elements y position
     * @param mouseX the mouse X position
     * @param mouseY the mouse Y position
     */
    default void postDraw(int x, int y, int mouseX, int mouseY) {
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
    default boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
        return true;
    }

    default boolean globalClicked(int mouseX, int mouseY, int mouseButton) {
        return false;//return true to cancel propagation
    }

    default boolean consumeScroll(int scrollAmount) {
        return false;
    }

    default int zLevel() {
        return 1;
    }
}
