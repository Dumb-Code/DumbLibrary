package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.dumbcode.dumblibrary.client.gui.ColourWheelSelector;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.json.objects.Constants;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.Mth;

import java.util.function.Consumer;

public class ImportanceColourPicker extends ColourWheelSelector {

    private boolean importanceSliderSelected;
    private float importance;

    public ImportanceColourPicker(int x, int y, int size, GeneticTint.Part current, Consumer<GeneticTint.Part> onChange) {
        super(x, y, size, null);
        this.onChange = (selector, r, g, b) -> onChange.accept(new GeneticTint.Part(r, g, b, current.getA(), (int) (this.importance * GeneticUtils.DEFAULT_COLOUR_IMPORTANCE)));
        this.importance = current.getImportance();
        this.setColour((int) (current.getR() * 255F), (int) (current.getG() * 255F), (int) (current.getB() * 255F));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if(mouseX >= this.x && mouseY >= this.y + this.size && mouseX < this.x + this.size && mouseY < this.y + this.height) {
            this.importanceSliderSelected = true;
            this.importance = (float) ((mouseX - this.x) / this.size);
            this.onChange();
        } else {
            this.importanceSliderSelected = false;
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double changeX, double changeY) {
        if(this.importanceSliderSelected) {
            this.importance = Mth.clamp((float) ((mouseX - this.x) / this.size), 0, 1);
            this.onChange();
        }
        super.onDrag(mouseX, mouseY, changeX, changeY);
    }

    @Override
    public void renderButton(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
//        stack.fill(this.x, this.y+this.height, this.x+this.width, this.y+this.height+17, 0xFF000000 | this.calculateColor());
        super.renderButton(stack, mouseX, mouseY, ticks);

        stack.fill(this.x, this.y+this.size+6, this.x+this.width, this.y+this.size+11, 0xFF000000);
        int startX = (int) (this.x + this.importance * this.width - 2);
        stack.fill(startX, this.y+this.size, startX+4, this.y+this.size+17, 0xFF000000);
    }
}
