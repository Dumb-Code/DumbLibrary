package net.dumbcode.dumblibrary.client.animation.objects;

import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.List;

public class RenderAnimatableCube implements AnimationLayer.AnimatableCube {

    private final AdvancedModelRenderer cube;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    private final float dimensionX;
    private final float dimensionY;
    private final float dimensionZ;

    public RenderAnimatableCube(AdvancedModelRenderer cube) {
        this.cube = cube;
        ModelBox box = ObfuscationReflectionHelper.<List<ModelBox>, ModelRenderer>getPrivateValue(ModelRenderer.class, cube, "cubeList", "field_78804" + "_l").get(0); //TODO: remove this god awful method of getting the offsets

        this.offsetX = box.posX1;
        this.offsetY = box.posY1;
        this.offsetZ = box.posZ1;

        this.dimensionX = box.posX2 - box.posX1;
        this.dimensionY = box.posY2 - box.posY1;
        this.dimensionZ = box.posZ2 - box.posZ1;
    }

    @Override
    public float getDefaultPositionX() {
        return this.cube.defaultPositionX;
    }

    @Override
    public float getDefaultPositionY() {
        return this.cube.defaultPositionY;
    }

    @Override
    public float getDefaultPositionZ() {
        return this.cube.defaultPositionZ;
    }

    @Override
    public float getRotationPointX() {
        return this.cube.rotationPointX;
    }

    @Override
    public float getRotationPointY() {
        return this.cube.rotationPointY;
    }

    @Override
    public float getRotationPointZ() {
        return this.cube.rotationPointZ;
    }

    @Override
    public float getDefaultRotationX() {
        return this.cube.defaultRotationX;
    }

    @Override
    public float getDefaultRotationY() {
        return this.cube.defaultRotationY;
    }

    @Override
    public float getDefaultRotationZ() {
        return this.cube.defaultRotationZ;
    }

    @Override
    public float getActualRotationX() {
        return this.cube.rotateAngleX;
    }

    @Override
    public float getActualRotationY() {
        return this.cube.rotateAngleY;
    }

    @Override
    public float getActualRotationZ() {
        return this.cube.rotateAngleZ;
    }

    @Override
    public float getOffsetX() {
        return this.offsetX;
    }

    @Override
    public float getOffsetY() {
        return this.offsetY;
    }

    @Override
    public float getOffsetZ() {
        return this.offsetZ;
    }

    @Override
    public float getDimensionX() {
        return this.dimensionX;
    }

    @Override
    public float getDimensionY() {
        return this.dimensionY;
    }

    @Override
    public float getDimensionZ() {
        return this.dimensionZ;
    }

    @Override
    public void setPositionX(float positionX) {
        this.cube.rotationPointX += positionX;
    }

    @Override
    public void setPositionY(float positionY) {
        this.cube.rotationPointY += positionY;

    }

    @Override
    public void setPositionZ(float positionZ) {
        this.cube.rotationPointZ += positionZ;
    }

    @Override
    public void setRotationX(float rotationX) {
        this.cube.rotateAngleX += rotationX;
    }

    @Override
    public void setRotationY(float rotationY) {
        this.cube.rotateAngleY += rotationY;
    }

    @Override
    public void setRotationZ(float rotationZ) {
        this.cube.rotateAngleY += rotationZ;
    }

    @Override
    public void reset() {
        this.cube.resetToDefaultPose();
    }

    @Nullable
    @Override
    public AnimationLayer.AnimatableCube getParent() {
        return this.cube.getParent() == null ? null : new RenderAnimatableCube(this.cube.getParent());
    }

    @Override
    public Vec3d getModelPos(Vec3d recurseValue) {
        return AnimationLayer.AnimatableCube.super.getModelPos(recurseValue);
    }
}
