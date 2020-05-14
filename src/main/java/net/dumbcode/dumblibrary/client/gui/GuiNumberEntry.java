package net.dumbcode.dumblibrary.client.gui;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;

public class GuiNumberEntry {

    private static final int BUTTON_WIDTH = 20;
    private static final int PADDING = 3;

    private final int id;

    private final double defaultScale;
    private final int decimalPlace;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final GuiTextField textField;
    private final GuiButton topButton;
    private final GuiButton bottomButton;

    private final ObjIntConsumer<GuiNumberEntry> listener;

    @Getter
    private double value;

    @Getter
    private int ticksSinceChanged;

    @Getter
    @Setter
    private boolean syncedSinceEdit = true;

    public GuiNumberEntry(int id, double currentValue, double defaultScale, int decimalPlace, int x, int y, int width, int height, Consumer<GuiButton> buttonConsumer, ObjIntConsumer<GuiNumberEntry> listener) {
        this.id = id;

        this.decimalPlace = decimalPlace;
        this.defaultScale = defaultScale;
        this.value = currentValue;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, x-width/2, y-height/2, width-BUTTON_WIDTH-PADDING, height);
        this.topButton = new GuiButton(0, x + width/2 - BUTTON_WIDTH, y-height/2, BUTTON_WIDTH, height/2, "+");
        this.bottomButton = new GuiButton(0, x + width/2 - BUTTON_WIDTH, y, BUTTON_WIDTH, height/2, "-");
        this.listener = listener;

        buttonConsumer.accept(this.topButton);
        buttonConsumer.accept(this.bottomButton);

        this.onChange(false, true);

    }

    public void render() {
        this.textField.drawTextBox();
    }

    public void updateEntry() {
        this.textField.updateCursorCounter();
        this.ticksSinceChanged++;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean focused = this.textField.isFocused();
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        if(focused && !this.textField.isFocused()) {
            this.onChange(false, true);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        this.textField.textboxKeyTyped(typedChar, keyCode);
        try {
            this.value = Double.parseDouble(this.textField.getText());
            this.onChange(true, false);
        } catch (NumberFormatException ignored) {
        }
    }

    public void buttonClicked(GuiButton button) {
        if(button == this.topButton) {
            this.addScaled(1);
        }
        if(button == this.bottomButton) {
            this.addScaled(-1);
        }
    }
    public void setValue(double value, boolean listener) {
        this.value = value;
        this.onChange(listener, true);
    }

    public boolean mouseOver(int mouseX, int mouseY) {
        return Math.abs(this.x - mouseX) <= this.width/2 && Math.abs(this.y - mouseY) <= this.height/2;
    }

    public void handleMouseInput(int width, int height) {
        int mouseX = Mouse.getEventX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getEventY() * height / Minecraft.getMinecraft().displayHeight - 1;
        if(this.mouseOver(mouseX, mouseY)) {
            int mouseInput = Mouse.getEventDWheel();
            if (mouseInput != 0) {
                this.addScaled(mouseInput < 0 ? -1 : 1);
            }
        }
    }

    private void addScaled(double amount) {
        if(!GuiScreen.isAltKeyDown()) {
            amount *= this.defaultScale;
        }
        if(GuiScreen.isCtrlKeyDown()) {
            amount *= 2;
        }
        if(GuiScreen.isShiftKeyDown()) {
            amount /= 2;
        }
        this.value += amount;
        this.onChange(true, true);
    }

    private void onChange(boolean updateListener, boolean updateTextField) {
        if(updateListener) {
            this.ticksSinceChanged = 0;
            this.syncedSinceEdit = false;
            this.listener.accept(this, this.id);
        }
        if(updateTextField && !this.textField.isFocused()) {
            double pow = Math.pow(10, this.decimalPlace);
            double val = Math.round(this.value * pow) / pow;
            this.textField.setText(MathUtils.ensureTrailingZeros(val, this.decimalPlace));
        }
    }
}
