package net.dumbcode.dumblibrary.server.animation.interpolation;

import net.dumbcode.dumblibrary.server.animation.objects.CubeWrapper;

import javax.vecmath.Vector3f;

public class LinearInterpolation implements Interpolation {

    @Override
    public float[] getInterpPos(CubeWrapper cube, float currentInterp) {
        Vector3f np = cube.getRotationPoint();
        Vector3f pp = cube.getPrevRotationPoint();

        return new float[] {
                pp.x + (np.x - pp.x) * currentInterp,
                pp.y + (np.y - pp.y) * currentInterp,
                pp.z + (np.z - pp.z) * currentInterp
        };
    }

    @Override
    public float[] getInterpRot(CubeWrapper cube, float currentInterp) {
        Vector3f cr = cube.getRotation();
        Vector3f pr = cube.getPrevRotation();

        return new float[] {
                pr.x + (cr.x - pr.x) * currentInterp,
                pr.y + (cr.y - pr.y) * currentInterp,
                pr.z + (cr.z - pr.z) * currentInterp
        };
    }
}
