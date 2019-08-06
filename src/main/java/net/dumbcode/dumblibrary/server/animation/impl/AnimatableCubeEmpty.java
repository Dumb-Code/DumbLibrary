package net.dumbcode.dumblibrary.server.animation.impl;

import net.dumbcode.dumblibrary.server.animation.AnimatableCube;

import javax.annotation.Nullable;

public class AnimatableCubeEmpty implements AnimatableCube {

    public static final AnimatableCubeEmpty INSTANCE = new AnimatableCubeEmpty();

    @Override
    public float[] getDefaultRotationPoint() {
        return new float[0];
    }

    @Override
    public float[] getActualRotationPoint() {
        return new float[0];
    }

    @Override
    public float[] getDefaultRotation() {
        return new float[0];
    }

    @Override
    public float[] getActualRotation() {
        return new float[0];
    }

    @Override
    public float[] getOffset() {
        return new float[0];
    }

    @Override
    public float[] getDimension() {
        return new float[0];
    }

    @Override
    public void addRotationPoint(float pointX, float pointY, float pointZ) {

    }

    @Override
    public void setRotationPoint(float pointX, float pointY, float pointZ) {

    }

    @Override
    public void addRotation(float rotationX, float rotationY, float rotationZ) {

    }

    @Override
    public void setRotation(float rotationX, float rotationY, float rotationZ) {

    }

    @Override
    public void reset() {

    }

    @Nullable
    @Override
    public AnimatableCube getParent() {
        return null;
    }
}
