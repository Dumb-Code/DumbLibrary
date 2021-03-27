package net.dumbcode.dumblibrary.client.model.dcm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.dumblibrary.server.info.ServerAnimatableCube;
import net.dumbcode.studio.animation.instance.AnimatedCube;
import net.dumbcode.studio.model.CubeInfo;
import net.minecraft.client.renderer.model.ModelRenderer;

public class DCMModelRenderer extends ModelRenderer implements AnimatedReferenceCube {

    private float texWidth = 64.0F;
    private float texHeight = 32.0F;

    private boolean growDirty = true;
    private final CubeInfo info;

    private float growX;
    private float growY;
    private float growZ;

    private ModelRenderer.ModelBox box;

    @Getter
    private final String name;

    @Setter
    private boolean hideButShowChildren;

    private final DCMModelRenderer parent;

    public DCMModelRenderer(DCMModel model, DCMModelRenderer parent, CubeInfo info) {
        super(model);
        model.getCubeNameMap().put(info.getName(), this);
        this.parent = parent;
        this.texWidth = model.texWidth;
        this.texHeight = model.texHeight;
        this.info = info;
        this.name = info.getName();
        for (CubeInfo child : info.getChildren()) {
            this.addChild(new DCMModelRenderer(model, this, child));
        }
    }

    @Override
    public CubeInfo getInfo() {
        return this.info;
    }

    @Override
    public DCMModelRenderer getParent() {
        return this.parent;
    }

    @Override
    public void setRotation(float x, float y, float z) {
        this.xRot = x;
        this.yRot = y;
        this.zRot = z;
    }

    @Override
    public float[] getRotation() {
        return new float[] { this.xRot, this.yRot, this.zRot };
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public float[] getPosition() {
        return new float[] { this.x, this.y, this.z };
    }

    @Override
    public void setCubeGrow(float x, float y, float z) {
        this.growX = x;
        this.growY = y;
        this.growZ = z;
        this.growDirty = true;
    }

    @Override
    public float[] getCubeGrow() {
        return new float[] { this.growX, this.growY, this.growZ };
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

    public void resetRotations() {
        float[] rotation = this.info.getRotation();
        this.xRot = rotation[0];
        this.yRot = rotation[1];
        this.zRot = rotation[2];
    }

    public void resetRotationPoint() {
        float[] position = this.info.getRotationPoint();
        this.x = position[0];
        this.y = position[1];
        this.z = position[2];
    }
}
