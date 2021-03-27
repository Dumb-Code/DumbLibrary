package net.dumbcode.dumblibrary.server.animation;

import net.dumbcode.studio.animation.instance.AnimatedCube;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;

public interface AnimatedReferenceCube extends AnimatedCube {
    float[] getPosition();
    float[] getRotation();
    float[] getCubeGrow();

    @Nullable
    AnimatedReferenceCube getParent();
}
