package net.dumbcode.dumblibrary.server.utils;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

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
        translation.setTranslation(new Vector3f(floats[0], floats[1], floats[2]));
        matrix.mul(translation);
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
        rotation.setRotation(new AxisAngle4f(x, y, z, angle));
        matrix.mul(rotation);
    }

    /**
     * Scales a matrix stack by an angle
     *
     * @param matrix The matrix of which to be scale
     * @param floats an array of length 3, defined as [x, y, z]
     */
    public static void scale(Matrix4f matrix, float... floats) {
        Matrix4f scale = new Matrix4f();
        scale.m00 = floats[0];
        scale.m11 = floats[1];
        scale.m22 = floats[2];
        scale.m33 = 1;
        matrix.mul(scale);
    }

}
