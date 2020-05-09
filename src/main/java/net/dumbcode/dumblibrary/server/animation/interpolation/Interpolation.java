package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.objects.CubeWrapper;

/**
 * An interface that allows for custom
 * interpolations for animations.
 */
public interface Interpolation {

    /**
     * Interpolation for the cube position.
     * @param cube cube being interpolated.
     * @param currentInterp how far the animation is in the current pose.
     * @return updated values for each position (x,y,z)
     */
    float[] getInterpPos(CubeWrapper cube, float currentInterp);

    /**
     * Interpolation for the cube rotation.
     * @param cube cube being interpolated.
     * @param currentInterp how far the animation is in the current pose.
     * @return updated values for each position (x,y,z)
     */
    float[] getInterpRot(CubeWrapper cube, float currentInterp);
}
