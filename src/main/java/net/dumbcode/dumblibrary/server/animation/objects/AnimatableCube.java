package net.dumbcode.dumblibrary.server.animation.objects;

import javax.annotation.Nullable;

public interface AnimatableCube {
    float[] getDefaultRotationPoint();

    float[] getActualRotationPoint();

    float[] getDefaultRotation();

    float[] getActualRotation();

    float[] getOffset();

    float[] getDimension();

    void addRotationPoint(float pointX, float pointY, float pointZ);
    void setRotationPoint(float pointX, float pointY, float pointZ);

    void addRotation(float rotationX, float rotationY, float rotationZ);
    void setRotation(float rotationX, float rotationY, float rotationZ);

    void reset();

    @Nullable
    AnimatableCube getParent();
}
