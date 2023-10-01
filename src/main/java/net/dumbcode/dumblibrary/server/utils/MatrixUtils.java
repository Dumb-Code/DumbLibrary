package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.util.math.vector.Matrix4f;
import org.joml.Vector3f;

public class MatrixUtils {

    /**
     * Translate a matrix stack
     *
     * @param matrix  the matrix of which to translate
     * @param floats an array of length 3 defined as [x, y, z]
     */
    public static void translate(Matrix4f matrix, float... floats) {
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(floats[0], floats[1], floats[2]);
        matrix.multiply(translation);
    }

    /**
     * Rotate a matrix stack by an angle
     *
     * @param matrix The matrix of which to be rotate
     * @param angle the angle, in radians, of which to rotate it by
     * @param x     the x magnitude. 1 or 0
     * @param y     the y magnitude. 1 or 0
     * @param z     the z magnitude. 1 or 0
     */
    public static void rotate(Matrix4f matrix, float angle, float x, float y, float z) {
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.multiply(new Vector3f(x, y, z).rotation(angle));
        matrix.multiply(rotation);
    }

    /**
     * Scales a matrix stack by an angle
     *
     * @param matrix The matrix of which to be scale
     * @param floats an array of length 3, defined as [x, y, z]
     */
    public static void scale(Matrix4f matrix, float... floats) {
        matrix.multiply(Matrix4f.createScaleMatrix(floats[0], floats[1], floats[2]));
    }

}
