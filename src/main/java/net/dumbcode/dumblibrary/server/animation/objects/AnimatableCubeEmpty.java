package net.dumbcode.dumblibrary.server.animation.objects;

import javax.annotation.Nullable;

public final class AnimatableCubeEmpty implements AnimatableCube {

    public static final AnimatableCubeEmpty INSTANCE = new AnimatableCubeEmpty();

    private AnimatableCubeEmpty() {

    }

    @Override
    public float[] getDefaultRotationPoint() {
        return new float[3];
    }

    @Override
    public float[] getActualRotationPoint() {
        return new float[3];
    }

    @Override
    public float[] getDefaultRotation() {
        return new float[3];
    }

    @Override
    public float[] getActualRotation() {
        return new float[3];
    }

    @Override
    public float[] getOffset() {
        return new float[3];
    }

    @Override
    public float[] getDimension() {
        return new float[3];
    }

    @Override
    public void addRotationPoint(float pointX, float pointY, float pointZ) {
        //NO OP
    }

    @Override
    public void setRotationPoint(float pointX, float pointY, float pointZ) {
        //NO OP
    }

    @Override
    public void addRotation(float rotationX, float rotationY, float rotationZ) {
        //NO OP
    }

    @Override
    public void setRotation(float rotationX, float rotationY, float rotationZ) {
        //NO OP
    }

    @Override
    public void reset() {
        //NO OP
    }

    @Nullable
    @Override
    public AnimatableCube getParent() {
        return null;
    }
}