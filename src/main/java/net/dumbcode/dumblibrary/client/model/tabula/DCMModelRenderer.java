package net.dumbcode.dumblibrary.client.model.tabula;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Setter;
import net.dumbcode.studio.animation.instance.AnimatedCube;
import net.dumbcode.studio.model.CubeInfo;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class DCMModelRenderer extends ModelRenderer implements AnimatedCube {

    private float texWidth = 64.0F;
    private float texHeight = 32.0F;

    private boolean growDirty = true;
    private final CubeInfo info;

    private float growX;
    private float growY;
    private float growZ;

    private ModelRenderer.ModelBox box;

    @Setter
    private boolean hideButShowChildren;

    public DCMModelRenderer(DCMModel model, CubeInfo info) {
        super(model);
        model.getCubeNameMap().put(info.getName(), this);
        this.texWidth = model.texWidth;
        this.texHeight = model.texHeight;
        this.info = info;
        for (CubeInfo child : info.getChildren()) {
            this.addChild(new DCMModelRenderer(model, child));
        }
    }

    @Override
    public CubeInfo getInfo() {
        return this.info;
    }

    @Override
    public void setRotation(float x, float y, float z) {
        this.xRot = x;
        this.yRot = y;
        this.zRot = z;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void setCubeGrow(float x, float y, float z) {
        this.growX = x;
        this.growY = y;
        this.growZ = z;
        this.growDirty = true;
    }
    
    @Override
    public void render(MatrixStack stack, IVertexBuilder buffer, int light, int overlay, float r, float g, float b, float a) {
        if(this.growDirty) {
            this.box = new ModelRenderer.ModelBox(
                this.info.getTextureOffset()[0], this.info.getTextureOffset()[0],
                this.info.getOffset()[0], this.info.getOffset()[1], this.info.getOffset()[2],
                this.info.getDimensions()[0], this.info.getDimensions()[1], this.info.getDimensions()[2],
                this.growX, this.growY, this.growZ,
                this.info.isTextureMirrored(),
                this.texWidth, this.texHeight
            );
            this.growDirty = false;
            this.cubes.clear();
            this.cubes.add(this.box);
        }
        if(this.hideButShowChildren) {
            this.cubes.clear();
        }
        super.render(stack, buffer, light, overlay, r, g, b, a);
        if(this.hideButShowChildren) {
            this.cubes.add(this.box);
        }
    }
}
