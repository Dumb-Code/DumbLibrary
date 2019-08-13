package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.AllArgsConstructor;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Float suppliers are currently only used in 2 context. To control the speed of the animation, and to control the
 * factor of which the angles are rotated. 90 degrees with a float supplier of 0.5 would yield 45 degrees.
 * <br>
 * A use case for this is for walking animations, whereas the the speed and degree of the animations is determined by the speed of the ecs.
 */
@AllArgsConstructor
@GameRegistry.ObjectHolder(DumbLibrary.MODID)
public class AnimationFactor extends IForgeRegistryEntry.Impl<AnimationFactor> {
    public static final AnimationFactor DEFAULT = new AnimationFactor((access, type, partialTicks) -> 1F).setRegistryName("default");
    private final FactorFunction function;

    public float getDegree(ComponentAccess access, Type type, float partialTicks) {
        return this.function.getDegree(access, type, partialTicks);
    }

    public interface FactorFunction {
        float getDegree(ComponentAccess access, Type type, float partialTicks);
    }

    public enum Type {
        SPEED, ANGLE;

        public boolean isSpeed() {
            return this == SPEED;
        }

        public boolean isAngle() {
            return this == ANGLE;
        }
    }

}
