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
    void draw(int x, int y, int mouseX, int mouseY);

    /**
     * Called when the entry is clicked
     *
     * @param relMouseX the relative mouse's x position for this entry
     * @param relMouseY the relative mouse's y position for this entry
     * @param mouseX the actual mouse's x position
     * @param mouseY the actual mouse's y position
     */
    default void onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {

    }
}
