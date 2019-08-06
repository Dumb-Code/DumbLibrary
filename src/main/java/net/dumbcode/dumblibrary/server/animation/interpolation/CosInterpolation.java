package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.AnimationLayer;

import javax.vecmath.Vector3f;

public class CosInterpolation implements Interpolation {

    @Override
    public float[] getInterpPos(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f np = cube.getRotationPoint();
        Vector3f pp = cube.getPrevRotationPoint();

        return new float[]{
                cosineInterpolate(pp.x, np.x, currentInterp),
                cosineInterpolate(pp.y, np.y, currentInterp),
                cosineInterpolate(pp.z, np.z, currentInterp)
        };
    }

    @Override
    public float[] getInterpRot(AnimationLayer.CubeWrapper cube, float currentInterp) {
        Vector3f cr = cube.getRotation();
        Vector3f pr = cube.getPrevRotation();

        return new float[] {
                cosineInterpolate(pr.x, cr.x, currentInterp),
                cosineInterpolate(pr.y, cr.y, currentInterp),
                cosineInterpolate(pr.z, cr.z, currentInterp)
        };
    }

    /**
     * An implementation of Cosine interpolation.
     * @see <a href="http://paulbourke.net/miscellaneous/interpolation/"paulbourke interpolation</a>
     * @param p0 previous position.
     * @param p1 current position.
     * @param ci current interpolation
     * @return updated position.
     */
    private float cosineInterpolate(float p0, float p1, float ci) {
        double mu2 = (1-Math.cos(ci*Math.PI))/2;
        return (float) (p0*(1-mu2)+p1*mu2);
    }
}
