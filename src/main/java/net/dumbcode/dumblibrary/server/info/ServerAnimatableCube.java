package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

public class ServerAnimatableCube implements AnimationLayer.AnimatableCube {

    private final ServerAnimatableCube parent;

    private final TabulaModelInformation.Cube cube;

    private Vector3f rotationPoint;
    private Vector3f rotation;

    public ServerAnimatableCube(ServerAnimatableCube parent, TabulaModelInformation.Cube cube) {
        this.parent = parent;
        this.cube = cube;
        this.reset();
    }

    @Override
    public float[] getDefaultRotationPoint() {
        return this.cube.getRotationPoint();
    }

    @Override
    public float[] getRotationPoint() {
        return new float[]{ this.rotationPoint.x, this.rotationPoint.y, this.rotationPoint.z };
    }

    @Override
    public float[] getDefaultRotation() {
        return this.cube.getRotation();
    }

    @Override
    public float[] getActualRotation() {
        return new float[]{ this.rotation.x, this.rotation.y, this.rotation.z };
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
        this.rotationPoint.x += pointX;
        this.rotationPoint.y += pointY;
        this.rotationPoint.z += pointZ;
    }

    @Override
    public void addRotation(float rotationX, float rotationY, float rotationZ) {
        this.rotation.x += rotationX;
        this.rotation.y += rotationY;
        this.rotation.z += rotationZ;
    }

    @Override
    public void reset() {
        this.rotationPoint = new Vector3f(this.getDefaultRotationPoint());
        this.rotation = new Vector3f(this.getDefaultRotation());
    }

    @Nullable
    @Override
    public AnimationLayer.AnimatableCube getParent() {
        return this.parent;
    }
}
