package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.studio.model.CubeInfo;

import java.util.Arrays;

public class ServerAnimatableCube implements AnimatedReferenceCube {

    private final ServerAnimatableCube parent;

    private final CubeInfo cube;

    private final float[] position;
    private final float[] rotation;
    private final float[] cubeGrow;

    public ServerAnimatableCube(ServerAnimatableCube parent, CubeInfo cube) {
        this.parent = parent;
        this.cube = cube;

        this.position = Arrays.copyOf(cube.getRotationPoint(), 3);
        this.rotation = Arrays.copyOf(cube.getRotation(), 3);
        this.cubeGrow = Arrays.copyOf(cube.getCubeGrow(), 3);
    }

    @Override
    public CubeInfo getInfo() {
        return this.cube;
    }

    @Override
    public ServerAnimatableCube getParent() {
        return parent;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.position[0] = x;
        this.position[1] = y;
        this.position[2] = z;
    }

    @Override
    public void setRotation(float x, float y, float z) {
        this.rotation[0] = x;
        this.rotation[1] = y;
        this.rotation[2] = z;
    }

    @Override
    public void setCubeGrow(float x, float y, float z) {
        this.cubeGrow[0] = x;
        this.cubeGrow[1] = y;
        this.cubeGrow[2] = z;
    }

    @Override
    public float[] getPosition() {
        return position;
    }

    @Override
    public float[] getRotation() {
        return rotation;
    }

    @Override
    public float[] getCubeGrow() {
        return cubeGrow;
    }

    public CubeInfo getCube() {
        return cube;
    }
}
