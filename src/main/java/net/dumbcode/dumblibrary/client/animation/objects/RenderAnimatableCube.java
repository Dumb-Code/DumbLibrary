package net.dumbcode.dumblibrary.client.animation.objects;

import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class RenderAnimatableCube implements AnimationLayer.AnimatableCube {

    private final AdvancedModelRenderer cube;

    public RenderAnimatableCube(AdvancedModelRenderer cube) {
        this.cube = cube;
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
