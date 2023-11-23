package net.dumbcode.dumblibrary.client.gui;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.util.math.Mth;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class ColourWheelSelector extends Widget {

    private static ShaderInstance shaderManager;

    private float lightness = 0F;
    private boolean wheelSelected;
    private boolean sliderSelected;

    private Vector3i selectedPoint = new Vector3i(0, 0, 0);

    protected OnChange onChange;

    protected final int size;

    public ColourWheelSelector(int x, int y, int size, OnChange onChange) {
        super(x, y, size, size, Component.literal(":)"));
        this.size = size - 17;
        this.onChange = onChange;
        if(shaderManager == null) {
            try {
                shaderManager = new ShaderInstance(Minecraft.getInstance().getResourceManager(), DumbLibrary.MODID + ":colorwheel");
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to load color wheel shader :/", e);
            }
        }
    }

    @Override
    public void renderButton(GuiGraphics stack, int mouseX, int mouseY, float ticks) {

        stack.fill(this.x, this.y, this.x+this.width, this.y+this.height, 0xFF000000 | this.calculateColor());

        RenderSystem.enableBlend();
        if(shaderManager != null) {

            shaderManager.safeGetUniform("lightness").set(1F - this.lightness);

            shaderManager.apply();

            int centerX = this.x + this.size/2;
            int centerY = this.y + this.size/2;

            int radii = this.size/2;

            BufferBuilder buff = Tesselator.getInstance().getBuilder();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.vertex(centerX - radii, centerY - radii, 0).uv(0, 0).endVertex();
            buff.vertex(centerX - radii, centerY + radii, 0).uv(0, 1).endVertex();
            buff.vertex(centerX + radii, centerY + radii, 0).uv(1, 1).endVertex();
            buff.vertex(centerX + radii, centerY - radii, 0).uv(1, 0).endVertex();

            Tesselator.getInstance().end();
            shaderManager.clear();
        }

        int halfSliderWidth = 6;

        stack.fill(this.x + this.size + halfSliderWidth + 3, this.y + 5, this.x + this.size + halfSliderWidth + 7, this.y + this.height - 5, 0xFF000000);
        stack.fill(this.x + this.size + 5, (int) (this.y + 2 + (this.getHeight()-10)*this.lightness), this.x + this.size + 2*halfSliderWidth + 5, (int) (y + 8 + (this.getHeight()-10)*this.lightness), 0xFF000000);


        int centerX = x + this.size/2;
        int centerY = y + this.size/2;
        RenderUtils.renderBorderExclusive(stack, centerX + this.selectedPoint.getX() - 2, centerY + this.selectedPoint.getY() - 2, centerX + this.selectedPoint.getX() + 2, centerY + this.selectedPoint.getY() + 2, 2, -1);

    }

    public int calculateColor() {
        double theta = -Math.atan2(this.selectedPoint.getY(), this.selectedPoint.getX()) - Math.PI/2D;
        double brightness = Math.min(1F, Math.sqrt(this.selectedPoint.getX()*this.selectedPoint.getX() + this.selectedPoint.getY()*this.selectedPoint.getY()) / (this.height/2D));
        return ColourUtils.HSBtoRGB((float) (theta / (2*Math.PI)), (float) brightness, 1F - this.lightness);
    }

    public ColourWheelSelector setColour(int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8 ) & 255;
        int b = (color      ) & 255;
        return this.setColour(r, g, b);
    }

    public ColourWheelSelector setColour(int r, int g, int b) {
        float[] hsb = ColourUtils.RGBtoHSB(r, g, b);
        this.lightness = 1F - hsb[2];

        double theta = -2*Math.PI*hsb[0] - Math.PI/2D;
        double length = hsb[1] * this.size/2D;

        this.selectedPoint = new Vector3i((int)(length*Math.cos(theta)), (int)(length*Math.sin(theta)), 0);
        return this;
    }

    @Override
    public void playDownSound(SoundHandler p_230988_1_) {
        //No sound
    }


    @Override
    public void onClick(double mouseX, double mouseY) {
        int startX = x + this.size;
        int halfSliderWidth = 6;
        if(mouseX > startX - 2 && mouseX < startX + 2*halfSliderWidth + 2&& mouseY > y - 2 && mouseY < y + this.size-10 + 2) {
            this.lightness = (float) (mouseY - y) / (this.height-10);
            this.sliderSelected = true;
        } else {
            this.sliderSelected = false;
        }

        int centerX = x + this.size/2;
        int centerY = y + this.size/2;
        int radii = this.size/2;

        if((mouseX - centerX)*(mouseX - centerX) + (mouseY - centerY)*(mouseY - centerY) <= radii*radii) {
            this.wheelSelected = true;
            this.selectedPoint = new Vector3i(mouseX - centerX + 5, mouseY - centerY + 5, 0);
        } else {
            this.wheelSelected = false;
        }

        if(this.sliderSelected || this.wheelSelected) {
            this.onChange(); //TODO: on change
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double changeX, double changeY) {
        super.onDrag(mouseX, mouseY, changeX, changeY);
        if(this.sliderSelected) {
            this.lightness = Mth.clamp((float) (mouseY - y) / (this.height-10), 0F, 1F);
        }
        if(this.wheelSelected) {
            this.selectedPoint = new Vector3i(mouseX - x - this.size/2F + 5, mouseY - y - this.size/2F + 5, 0);
            double theta = Math.atan2(this.selectedPoint.getY(), this.selectedPoint.getX());
            double length = Math.min(Math.sqrt(this.selectedPoint.getX()*this.selectedPoint.getX() + this.selectedPoint.getY()*this.selectedPoint.getY()), this.size/2D);

            this.selectedPoint = new Vector3i((int) (length*Math.cos(theta)), (int) (length*Math.sin(theta)), 0);
        }

        if(this.sliderSelected || this.wheelSelected) {
            this.onChange();
        }
    }

    protected void onChange() {
        int color = this.calculateColor();
        float r = ((color >> 16) & 255) / 255F;
        float g = ((color >> 8 ) & 255) / 255F;
        float b = ((color      ) & 255) / 255F;
        this.onChange.onChange(this, r, g, b);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.sliderSelected = this.wheelSelected = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public interface OnChange {
        void onChange(ColourWheelSelector selector, float r, float g, float b);
    }
}
