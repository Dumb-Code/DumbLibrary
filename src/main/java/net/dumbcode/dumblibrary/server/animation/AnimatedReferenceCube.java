package net.dumbcode.dumblibrary.server.animation;

import net.dumbcode.studio.animation.instance.AnimatedCube;
import net.dumbcode.studio.model.RotationOrder;
import net.dumbcode.studio.util.EssentiallyACube;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public interface AnimatedReferenceCube extends AnimatedCube, EssentiallyACube {
    float[] getPosition();
    float[] getRotation();
    float[] getCubeGrow();

    @Override
    default float[] getOffset() {
        return this.getInfo().getOffset();
    }

    @Override
    default float[] getRotationPoint() {
        return this.getPosition();
    }

    @Override
    default int[] getDimensions() {
        return this.getInfo().getDimensions();
    }

    @Override
    default RotationOrder getRotationOrder() {
        return this.getInfo().getRotationOrder(); //Should always be ZYX
    }

    @Nullable
    AnimatedReferenceCube getParent();
}
