package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Getter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Float suppliers are currently only used in 2 context. To control the speed of the animation, and to control the
 * factor of which the angles are rotated. 90 degrees with a float supplier of 0.5 would yield 45 degrees.
 * <br>
 * A use case for this is for walking animations, whereas the the speed and degree of the animations is determined by the speed of the ecs.
 */
@Getter
public class AnimationFactor<T> {
    public static Map<String, AnimationFactor<?>> REGISTRY = new HashMap<>();
    public static final AnimationFactor<?> DEFAULT = new AnimationFactor<>("default", Void.class, (access, type, partialTicks) -> 1F).register();

    public AnimationFactor(String name, Class<T> baseClass, FactorFunction<T> function) {
        this.name = name;
        this.baseClass = baseClass;
        this.function = function;
    }

    public static <T> AnimationFactor<T> getDefault() {
        return (AnimationFactor<T>) DEFAULT;
    }

    private final String name;
    private final Class<T> baseClass;
    private final FactorFunction<T> function;

    public float tryApply(Object o, Type type, float partialTicks) {
        if(this.baseClass.isInstance(o)) {
            return this.function.getDegree(this.baseClass.cast(o), type, partialTicks);
        }
        return 1F;
    }

    public AnimationFactor<T> register() {
        REGISTRY.put(this.name, this);
        return this;
    }

    public static AnimationFactor<?> getFactor(String name) {
        return REGISTRY.getOrDefault(name, DEFAULT);
    }

    public interface FactorFunction<T> {
        float getDegree(T entity, Type type, float partialTicks);
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
