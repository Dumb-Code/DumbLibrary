package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Getter;

import javax.vecmath.Vector3f;

@Getter
public class CubeWrapper {
    private final Vector3f rotationPoint = new Vector3f();
    private final Vector3f prevRotationPoint = new Vector3f();
    private final Vector3f rotation = new Vector3f();
    private final Vector3f prevRotation = new Vector3f();


    public CubeWrapper(AnimatableCube box) {
        float[] point = box.getDefaultRotationPoint();
        float[] defaultRotation = box.getDefaultRotation();
        this.rotationPoint.x = this.prevRotationPoint.x = point[0];
        this.rotationPoint.y = this.prevRotationPoint.y = point[1];
        this.rotationPoint.z = this.prevRotationPoint.z = point[2];
        this.rotation.x = this.prevRotation.x = defaultRotation[0];
        this.rotation.y = this.prevRotation.y = defaultRotation[1];
        this.rotation.z = this.prevRotation.z = defaultRotation[2];
    }
}
