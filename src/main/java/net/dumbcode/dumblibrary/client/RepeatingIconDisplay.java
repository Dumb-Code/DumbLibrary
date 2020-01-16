package net.dumbcode.dumblibrary.client;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;

@AllArgsConstructor
public class RepeatingIconDisplay {

    private final float value;
    private final float maxValue;

    private final int iconSize;
    private final int iconsPerLine;
    private final float valuePerIcon;

    private final TriConsumer<Integer, Integer, Float> renderCallback;


    private int iconAmount() {
        return (int) Math.ceil(this.maxValue / this.valuePerIcon);
    }

    public int getWidth() {
        return Math.min(this.iconAmount(), this.iconsPerLine) * this.iconSize;
    }

    public int getHeight() {
        return (this.iconSize * this.iconAmount()) / this.iconsPerLine;
    }

   public void render(int xOffset, int yOffset) {
       for (int heart = 0; heart < this.iconAmount(); heart++) {
           int heartX = this.iconSize * (heart % this.iconsPerLine) - (int) (this.iconSize / this.valuePerIcon) + xOffset;
           int heartY = this.iconSize * (heart / this.iconsPerLine) + yOffset;

           float size =  Math.min(1F, (this.value - heart*this.valuePerIcon) / this.valuePerIcon);

           this.renderCallback.accept(heartX, heartY, size);
       }
   }
}

