package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.AnimationLayer;

import javax.vecmath.Vector3f;

public class SmoothStepInterpolation implements Interpolation {

    @Override
    public float[] getInterpPos(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f np = cube.getRotationPoint();
        Vector3f pp = cube.getPrevRotationPoint();

        return new float[]{
                smoothInterpolate(pp.x, np.x, currentInterp),
                smoothInterpolate(pp.y, np.y, currentInterp),
                smoothInterpolate(pp.z, np.z, currentInterp)
        };
    }

    @Override
    public float[] getInterpRot(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f cr = cube.getRotation();
        Vector3f pr = cube.getPrevRotation();

        return new float[] {
                smoothInterpolate(pr.x, cr.x, currentInterp),
                smoothInterpolate(pr.y, cr.y, currentInterp),
                smoothInterpolate(pr.z, cr.z, currentInterp)
        };
    }

    /**
     * An implementation of Smooth step interpolation.
     * @see <a href="http://sol.gfxile.net/interpolation/"interpolation</a>
     * @param p0 previous position.
     * @param p1 current position.
     * @param ci current interpolation
     * @return updated position.
     */
    private float smoothInterpolate(float p0, float p1, float ci) {
        float smooth = (float) (Math.pow(ci, 3) * (ci * (ci * 6 - 15) + 10));
        return (p0 * smooth) + (p1 * (1-smooth));
    }
}