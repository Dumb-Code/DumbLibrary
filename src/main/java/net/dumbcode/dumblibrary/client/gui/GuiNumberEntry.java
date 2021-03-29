package net.dumbcode.dumblibrary.client.gui;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class GuiNumberEntry extends Widget {

    private static final int BUTTON_WIDTH = 20;
    private static final int PADDING = 3;

    private final int id;

    private final double defaultScale;
    private final int decimalPlace;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final TextFieldWidget textField;
    private final Button topButton;
    private final Button bottomButton;

    private final ObjIntConsumer<GuiNumberEntry> listener;

    @Getter
    private double value;

    @Getter
    private int ticksSinceChanged;

    @Getter
    @Setter
    private boolean syncedSinceEdit = true;

    public GuiNumberEntry(int id, double currentValue, double defaultScale, int decimalPlace, int x, int y, int width, int height, Consumer<Widget> widgetConsumer, ObjIntConsumer<GuiNumberEntry> listener) {
        super(x, y, width, height, new StringTextComponent(""));

        this.id = id;
        this.decimalPlace = decimalPlace;
        this.defaultScale = defaultScale;
        this.value = currentValue;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.textField = new TextFieldWidget(Minecraft.getInstance().font, x-width/2, y-height/2, width-BUTTON_WIDTH-PADDING, height, new StringTextComponent(""));
        this.topButton = new ExtendedButton(x + width/2 - BUTTON_WIDTH, y-height/2, BUTTON_WIDTH, height/2, new StringTextComponent("+"), b -> this.addScaled(1));
        this.bottomButton = new ExtendedButton(x + width/2 - BUTTON_WIDTH, y, BUTTON_WIDTH, height/2, new StringTextComponent("-"), b -> this.addScaled(-1));
        this.listener = listener;

        widgetConsumer.accept(this.topButton);
        widgetConsumer.accept(this.bottomButton);
        widgetConsumer.accept(this.textField);

        this.onChange(false, true);

    }

    @Override
    public boolean isMouseOver(double x, double y) {
        double diffX = x - this.x;
        double diffY = y - this.y;
        return diffX >= 0 && diffX <= this.width && diffY >= 0 && diffY <= this.height;
    }

    public void tick() {
        this.textField.tick();
        this.ticksSinceChanged++;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean focused = this.textField.isFocused();
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        if(focused && !this.textField.isFocused()) {
            this.onChange(false, true);
        }
        return false;
    }

    public void setValue(double value, boolean listener) {
        this.value = value;
        this.onChange(listener, true);
    }

    public boolean mouseOver(double mouseX, double mouseY) {
        return Math.abs(this.x - mouseX) <= this.width/2D && Math.abs(this.y - mouseY) <= this.height/2D;
    }


    private void addScaled(double amount) {
        if(!Screen.hasAltDown()) {
            amount *= this.defaultScale;
        }
        if(Screen.hasControlDown()) {
            amount *= 2;
        }
        if(Screen.hasShiftDown()) {
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
            this.textField.setValue(MathUtils.ensureTrailingZeros(val, this.decimalPlace));
        }
    }
}
