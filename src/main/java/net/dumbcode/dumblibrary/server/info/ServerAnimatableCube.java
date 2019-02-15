package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.objects.AnimationLayer;
import net.ilexiconn.llibrary.client.model.tabula.container.TabulaCubeContainer;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

public class ServerAnimatableCube implements AnimationLayer.AnimatableCube {

    private final ServerAnimatableCube parent;

    private final TabulaCubeContainer cube;

    private Vector3f position;
    private Vector3f rotation;

    public ServerAnimatableCube(ServerAnimatableCube parent, TabulaCubeContainer cube) {
        this.parent = parent;
        this.cube = cube;
        this.reset();
    }

    @Override
    public float getDefaultPositionX() {
        return (float) this.cube.getPosition()[0];
    }

    @Override
    public float getDefaultPositionY() {
        return (float) this.cube.getPosition()[1];
    }

    @Override
    public float getDefaultPositionZ() {
        return (float) this.cube.getPosition()[2];
    }

    @Override
    public float getRotationPointX() {
        return (float) this.cube.getPosition()[0];
    }

    @Override
    public float getRotationPointY() {
        return (float) this.cube.getPosition()[1];
    }

    @Override
    public float getRotationPointZ() {
        return (float) this.cube.getPosition()[2];
    }

    @Override
    public float getDefaultRotationX() {
        return (float) Math.toRadians(this.cube.getRotation()[0]);
    }

    @Override
    public float getDefaultRotationY() {
        return (float) Math.toRadians(this.cube.getRotation()[1]);
    }

    @Override
    public float getDefaultRotationZ() {
        return (float) Math.toRadians(this.cube.getRotation()[2]);
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
    public float getOffsetX() {
        return (float) this.cube.getOffset()[0];
    }

    @Override
    public float getOffsetY() {
        return (float) this.cube.getOffset()[1];
    }

    @Override
    public float getOffsetZ() {
        return (float) this.cube.getOffset()[2];
    }

    @Override
    public float getDimensionX() {
        return this.cube.getDimensions()[0];
    }

    @Override
    public float getDimensionY() {
        return this.cube.getDimensions()[1];
    }

    @Override
    public float getDimensionZ() {
        return this.cube.getDimensions()[2];
    }

    @Override
    public void setPositionX(float positionX) {
        this.position.x += positionX;
    }

    @Override
    public void setPositionY(float positionY) {
        this.position.y += positionY;
    }

    @Override
    public void setPositionZ(float positionZ) {
        this.position.z += positionZ;
    }

    @Override
    public void setRotationX(float rotationX) {
        this.rotation.x += rotationX;
    }

    @Override
    public void setRotationY(float rotationY) {
        this.rotation.y += rotationY;
    }

    @Override
    public void setRotationZ(float rotationZ) {
        this.rotation.z += rotationZ;
    }

    @Override
    public void reset() {
        this.position = new Vector3f(this.getDefaultPositionX(), this.getDefaultPositionY(), this.getDefaultPositionZ());
        this.rotation = new Vector3f(this.getDefaultRotationX(), this.getDefaultRotationY(), this.getDefaultRotationZ());
    }

    @Nullable
    @Override
    public AnimationLayer.AnimatableCube getParent() {
        return this.parent;
    }
}
