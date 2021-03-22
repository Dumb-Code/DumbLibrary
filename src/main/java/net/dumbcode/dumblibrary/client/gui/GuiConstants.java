package net.dumbcode.dumblibrary.client.gui;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public interface GuiConstants {
    /**
     * This color is a white-ish color that is easy on the eyes
     */
    int NICE_WHITE = 0xFFF0F0F0;

    ResourceLocation ROTATION_RING_LOCATION = new ResourceLocation(DumbLibrary.MODID, "models/misc/rotation_ring.tbl");

    TranslationTextComponent LEFT_CLICK_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.left_click");
    TranslationTextComponent CONTROLS_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls");
    TranslationTextComponent MIDDLE_CLICK_DRAG_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.middle_click_drag");
    TranslationTextComponent MOVEMENT_KEYS_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.movement_keys");
    TranslationTextComponent ARROW_KEYS_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.arrow_keys");
    TranslationTextComponent TRACKPAD_ZOOM_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.trackpad_zoom");
    TranslationTextComponent MOUSE_WHEEL_TEXT = new TranslationTextComponent(DumbLibrary.MODID+".gui.controls.mouse_wheel");

    static boolean mouseOn(Button button, int mouseX, int mouseY) {
        return button.active && button.visible && mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.getWidth() && mouseY < button.y + button.getHeight();
    }
}
