package net.dumbcode.dumblibrary.server.utils;

import net.minecraft.util.math.vector.Vector3f;

public enum XYZAxis {
    X_AXIS(new Vector3f(1f, 0f, 0f)),
    Y_AXIS(new Vector3f(0f, 1f, 0f)),
    Z_AXIS(new Vector3f(0f, 0f, 1f)),
    NONE(new Vector3f(0f, 0f, 0f));

    private Vector3f axis;

    XYZAxis(Vector3f axis) {
        this.axis = axis;
    }

    public Vector3f getAxis() {
        return axis;
    }
}
