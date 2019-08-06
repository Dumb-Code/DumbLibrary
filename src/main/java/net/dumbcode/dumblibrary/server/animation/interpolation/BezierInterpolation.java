package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.AnimationLayer;

import javax.vecmath.Vector3f;

public class BezierInterpolation implements Interpolation {

    @Override
    public float[] getInterpPos(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f np = cube.getRotationPoint();
        Vector3f pp = cube.getPrevRotationPoint();

        return new float[]{
                binomialBezier(pp.x, np.x, currentInterp),
                binomialBezier(pp.y, np.y, currentInterp),
                binomialBezier(pp.z, np.z, currentInterp)
        };
    }

    @Override
    public float[] getInterpRot(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f cr = cube.getRotation();
        Vector3f pr = cube.getPrevRotation();

        return new float[] {
                binomialBezier(pr.x, cr.x, currentInterp),
                binomialBezier(pr.y, cr.y, currentInterp),
                binomialBezier(pr.z, cr.z, currentInterp)
        };
    }

    /**
     * An implementation of a binomial bezier curve.
     * @see <a href="https://javascript.info/bezier-curve"Bezier Curve</a>
     * @param p0 previous position.
     * @param p3 current position.
     * @param ci current interpolation
     * @return updated position.
     */
    private float binomialBezier(float p0, float p3, float ci) {
        return (float) (Math.pow(1-ci, 3) * p0 + 3 * Math.pow(1-ci, 2) *
                ci * (p0 + 0.1) + 3*(1-ci) * Math.pow(ci, 2) * (p0 - 0.1) + Math.pow(ci, 3) * p3);
    }
}
