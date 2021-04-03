package net.dumbcode.dumblibrary.client.gui;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.function.Consumer;

public class WrappedSlider extends Slider {

    private final Consumer<WrappedSlider> onChange;
    private final Runnable actualizeEdit;

    private double previousValue;
    private boolean previousDragging;

    public WrappedSlider(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, Consumer<WrappedSlider> onChange, Runnable actualizeEdit) {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, b -> {});
        this.onChange = onChange;
        this.actualizeEdit = actualizeEdit;
        this.previousValue = this.getValue();
    }

    @Override
    public void updateSlider() {
        super.updateSlider();
        if(Math.abs(this.getValue() - this.previousValue) > 0.001) {
            this.onChange.accept(this);
            this.previousValue = this.getValue();
        }
        if(this.previousDragging && !this.dragging) {
            this.actualizeEdit.run();
        }
        this.previousDragging = this.dragging;
    }

    @Override
    public void setValue(double d) {
        super.setValue(d);
        this.previousValue = d;
    }
}
