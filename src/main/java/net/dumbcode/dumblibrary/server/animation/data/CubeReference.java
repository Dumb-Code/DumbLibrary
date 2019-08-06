package net.dumbcode.dumblibrary.server.animation.data;

import lombok.Value;
import net.dumbcode.dumblibrary.server.tabula.TabulaModelInformation;

/**
 * A reference to the cube. Used to store positional/rotation data
 */
@Value
public class CubeReference {

    float rotationX;
    float rotationY;
    float rotationZ;

    float rotationPointX;
    float rotationPointY;
    float rotationPointZ;

    public static CubeReference fromCube(TabulaModelInformation.Cube cube) {
        float[] rotation = cube.getRotation();
        float[] rotationPoint = cube.getRotationPoint();
        return new CubeReference(
                rotation[0],
                rotation[1],
                rotation[2],
                rotationPoint[0],
                rotationPoint[1],
                rotationPoint[2]
        );
    }
}
