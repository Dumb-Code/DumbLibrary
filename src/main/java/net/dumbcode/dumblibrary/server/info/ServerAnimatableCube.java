package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

public class ServerAnimatableCube implements AnimationLayer.AnimatableCube {

    private final ServerAnimatableCube parent;

    private final Vector3f defaultPosition;
    private final Vector3f defaultRotation;
    private final Vector3f rotationPoint;

    private final Vector3f position;
    private final Vector3f rotation;

    public ServerAnimatableCube(ServerAnimatableCube parent, double[] defaultPosition, double[] defaultRotation, double[] rotationPoint) {
        this.parent = parent;

        this.defaultPosition = new Vector3f((float) defaultPosition[0], (float) defaultPosition[1], (float) defaultPosition[2]);
        this.defaultRotation = new Vector3f((float) Math.toRadians(defaultRotation[0]), (float) Math.toRadians(defaultRotation[1]), (float) Math.toRadians(defaultRotation[2]));
        this.rotationPoint = new Vector3f((float) rotationPoint[0], (float) rotationPoint[1], (float) rotationPoint[2]);

        this.position = new Vector3f(this.defaultPosition);
        this.rotation = new Vector3f(this.defaultRotation);
    }

    @Override
    public float getDefaultPositionX() {
        return this.defaultPosition.x;
    }

    @Override
    public float getDefaultPositionY() {
        return this.defaultPosition.y;
    }

    @Override
    public float getDefaultPositionZ() {
        return this.defaultPosition.z;
    }

    @Override
    public float getRotationPointX() {
        return this.rotationPoint.x;
    }

    @Override
    public float getRotationPointY() {
        return this.rotationPoint.y;
    }

    @Override
    public float getRotationPointZ() {
        return this.rotationPoint.z;
    }

    @Override
    public float getDefaultRotationX() {
        return this.defaultRotation.x;
    }

    @Override
    public float getDefaultRotationY() {
        return this.defaultRotation.y;
    }

    @Override
    public float getDefaultRotationZ() {
        return this.defaultRotation.z;
    }

    @Override
    public float getActualRotationX() {
        return this.rotation.x;
    }

    @Override
    public float getActualRotationY() {
        return this.rotation.y;
    }

    @Override
    public float getActualRotationZ() {
        return this.rotation.z;
    }

    @Override
    public void setPositionX(float positionX) {
        this.position.x = positionX + this.getDefaultPositionX();
    }

    @Override
    public void setPositionY(float positionY) {
        this.position.y = positionY + this.getDefaultPositionY();
    }

    @Override
    public void setPositionZ(float positionZ) {
        this.position.z = positionZ + this.getDefaultPositionZ();
    }

    @Override
    public void setRotationX(float rotationX) {
        this.rotation.x = rotationX + this.getDefaultRotationX();
    }

    @Override
    public void setRotationY(float rotationY) {
        this.rotation.y = rotationY + this.getDefaultRotationY();
    }

    @Override
    public void setRotationZ(float rotationZ) {
        this.rotation.z = rotationZ + this.getDefaultRotationZ();
    }

    @Nullable
    @Override
    public AnimationLayer.AnimatableCube getParent() {
        return this.parent;
    }
}
