package net.dumbcode.dumblibrary.client.model.tabula;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The model renderer for each cube taken from a tabula model.
 */
public class TabulaModelRenderer extends ModelRenderer implements AnimationLayer.AnimatableCube {
    @Getter private final TabulaModelInformation.Cube cube;

    public float scaleX, scaleY, scaleZ;
    public float cubeScaleX, cubeScaleY, cubeScaleZ;

    public boolean hideButShowChildren;
    public boolean scaleChildren;

    @Getter private TabulaModelRenderer parent;

    private int displayList;
    private boolean compiled;

    public TabulaModelRenderer(ModelBase model, TabulaModelInformation.Cube cube) {
        super(model, cube.getName());
        this.cube = cube;
    }

    @Override
    public void addChild(ModelRenderer renderer) {
        if(renderer instanceof TabulaModelRenderer) {
            ((TabulaModelRenderer) renderer).parent = this;
        }
        super.addChild(renderer);
    }


    public void resetRotations() {
        float[] rotations = this.cube.getRotation();
        this.rotateAngleX = rotations[0];
        this.rotateAngleY = rotations[1];
        this.rotateAngleZ = rotations[2];
    }

    public void resetRotationPoint() {
        float[] positions = this.cube.getRotationPoint();
        this.rotationPointX = positions[0];
        this.rotationPointY = positions[1];
        this.rotationPointZ = positions[2];
    }


    @Override
    public void render(float scale) {
        if (!this.isHidden) {
            if (this.showModel) {
                if (!this.compiled) {
                    this.compileDisplayList(scale);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate((float) Math.toDegrees(this.rotateAngleZ), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate((float) Math.toDegrees(this.rotateAngleY), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate((float) Math.toDegrees(this.rotateAngleX), 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.pushMatrix();
                if (this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F) {
                    GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
                }


                if(!this.hideButShowChildren) {
                    GlStateManager.pushMatrix();
                    if (this.cubeScaleX != 1.0F || this.cubeScaleY != 1.0F || this.cubeScaleZ != 1.0F) {
                        GlStateManager.scale(this.cubeScaleX, this.cubeScaleY, this.cubeScaleZ);
                    }
                    GlStateManager.callList(this.displayList);
                    GlStateManager.popMatrix();
                }

                if(!this.scaleChildren) {
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();
                }

                if (this.childModels != null) {
                    for (ModelRenderer childModel : this.childModels) {
                        childModel.render(scale);
                    }
                }

                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }

        }
    }

    @SideOnly(Side.CLIENT)
    private void compileDisplayList(float scale) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        for (ModelBox modelBox : this.cubeList) {
            modelBox.render(bufferbuilder, scale);
        }
        GlStateManager.glEndList();
        this.compiled = true;
    }

    @Override
    public float[] getDefaultRotationPoint() {
        return this.cube.getRotationPoint();
    }

    @Override
    public float[] getRotationPoint() {
        return new float[]{ this.rotationPointX, this.rotationPointY, this.rotationPointZ };
    }

    @Override
    public float[] getDefaultRotation() {
        return this.cube.getRotation();
    }

    @Override
    public float[] getActualRotation() {
        return new float[]{ this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ };
    }

    @Override
    public float[] getOffset() {
        return this.cube.getOffset();
    }

    @Override
    public float[] getDimension() {
        return this.cube.getDimension();
    }

    @Override
    public void addRotationPoint(float pointX, float pointY, float pointZ) {
        this.rotationPointX += pointX;
        this.rotationPointY += pointY;
        this.rotationPointZ += pointZ;
    }

    @Override
    public void addRotation(float rotationX, float rotationY, float rotationZ) {
        this.rotateAngleX += rotationX;
        this.rotateAngleY += rotationY;
        this.rotateAngleZ += rotationZ;
    }

    @Override
    public void reset() {
        this.resetRotations();
        this.resetRotationPoint();

        this.offsetX = this.offsetY = this.offsetZ = 0; //todo: add a defaultOffset?
        this.scaleX = this.scaleY = this.scaleZ = 1; //todo: add a defaultScale?
        this.cubeScaleX = this.cubeScaleY = this.cubeScaleZ = 1; //todo: add a defaultScale?

        this.hideButShowChildren = false;
        this.scaleChildren = false;
    }

    public void setParentedAngles(float scale) {
        if(this.parent != null) {
            this.parent.setParentedAngles(scale);
        }
        this.postRender(scale);
    }
}
