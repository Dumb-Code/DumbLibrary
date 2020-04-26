package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.CubeWrapper;

import javax.vecmath.Vector3f;

public class CosInterpolation implements Interpolation {

    @Override
    public float[] getInterpPos(CubeWrapper cube, float currentInterp) {
        Vector3f np = cube.getRotationPoint();
        Vector3f pp = cube.getPrevRotationPoint();

        return new float[]{
                CosineInterpolate(pp.x, np.x, currentInterp),
                CosineInterpolate(pp.y, np.y, currentInterp),
                CosineInterpolate(pp.z, np.z, currentInterp)
        };
    }

    @Override
    public float[] getInterpRot(CubeWrapper cube, float currentInterp) {
        Vector3f cr = cube.getRotation();
        Vector3f pr = cube.getPrevRotation();

        return new float[] {
                CosineInterpolate(pr.x, cr.x, currentInterp),
                CosineInterpolate(pr.y, cr.y, currentInterp),
                CosineInterpolate(pr.z, cr.z, currentInterp)
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
    private float CosineInterpolate(float p0, float p1, float ci) {
        double mu2 = (1-Math.cos(ci*Math.PI))/2;
        return (float) (p0*(1-mu2)+p1*mu2);
    }
}
