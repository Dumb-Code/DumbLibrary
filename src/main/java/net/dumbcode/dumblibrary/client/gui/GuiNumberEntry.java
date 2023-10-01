package net.dumbcode.dumblibrary.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.ObjIntConsumer;

public class GuiNumberEntry extends Widget implements INestedGuiEventHandler {

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

    @Nullable @Getter @Setter
    private IGuiEventListener focused;
    @Getter @Setter
    private boolean dragging;

    private boolean ignoreChange = false;

    private final List<Widget> children;

    public GuiNumberEntry(int id, double currentValue, double defaultScale, int decimalPlace, int x, int y, int width, int height, ObjIntConsumer<GuiNumberEntry> listener) {
        super(x, y, width, height, Component.literal(""));

        this.id = id;
        this.decimalPlace = decimalPlace;
        this.defaultScale = defaultScale;
        this.value = currentValue;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.textField = new TextFieldWidget(Minecraft.getInstance().font, x-width/2, y-height/2, width-BUTTON_WIDTH-PADDING, height, Component.literal(""));
        this.textField.setResponder(s -> {
            try {
                this.value = Double.parseDouble(s);
                this.onChange(true, false);
            } catch (NumberFormatException ignored) {
            }
        });
        this.topButton = new ExtendedButton(x + width/2 - BUTTON_WIDTH, y-height/2, BUTTON_WIDTH, height/2, Component.literal("+"), b -> this.addScaled(1));
        this.bottomButton = new ExtendedButton(x + width/2 - BUTTON_WIDTH, y, BUTTON_WIDTH, height/2, Component.literal("-"), b -> this.addScaled(-1));
        this.listener = listener;

        this.children = Collections.unmodifiableList(Lists.newArrayList(this.topButton, this.bottomButton, this.textField));

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
    public void setBlitOffset(int off) {
        super.setBlitOffset(off);
        for (Widget child : this.children) {
            child.setBlitOffset(off);
        }
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        for (Widget child : this.children) {
            child.render(stack, mouseX, mouseY, ticks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean focused = this.textField.isFocused();
        boolean ret = INestedGuiEventHandler.super.mouseClicked(mouseX, mouseY, mouseButton);
        if(focused && !this.textField.isFocused()) {
            this.onChange(false, true);
        }
        return ret;
    }

    public void setValue(double value, boolean listener) {
        this.value = value;
        this.onChange(listener, true);
    }

    public boolean mouseOver(double mouseX, double mouseY) {
        return Math.abs(this.x - mouseX) <= this.width/2D && Math.abs(this.y - mouseY) <= this.height/2D;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if(this.isMouseOver(x, y)) {
            this.addScaled(amount);
        }
        return INestedGuiEventHandler.super.mouseScrolled(x, y, amount);
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
        if(updateListener && !this.ignoreChange) {
            this.ticksSinceChanged = 0;
            this.syncedSinceEdit = false;
            this.listener.accept(this, this.id);
        }
        if(updateTextField && !this.textField.isFocused()) {
            double pow = Math.pow(10, this.decimalPlace);
            double val = Math.round(this.value * pow) / pow;
            this.ignoreChange = true;
            this.textField.setValue(MathUtils.ensureTrailingZeros(val, this.decimalPlace));
            this.ignoreChange = false;
        }
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        return this.children;
    }
}
