package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Getter;
import net.minecraft.entity.Entity;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * The wrapper for the {@link AnimationLayer}. Used to control multiple {@link AnimationLayer}s
 *
 * @param <E> the entity type this is used for
 */
public class AnimationRunWrapper<E extends Entity> {
    @Getter
    private final E entity;
    @Getter
    private final List<AnimationLayer<E>> layers;

    /**
     * @param entity the entity
     * @param layers a list of all animation layers to use
     */
    public AnimationRunWrapper(E entity, List<AnimationLayer<E>> layers) {
        this.entity = entity;
        this.layers = layers;
    }

    /**
     * Perform the Animations on all the animation passes
     *
     * @param entity          the entity
     * @param limbSwing       the limb swing
     * @param limbSwingAmount the limb swing amount
     * @param ticks           the age of the entity in ticks
     */
    public void performAnimations(E entity, float limbSwing, float limbSwingAmount, float ticks) {
        this.layers.forEach(l -> l.animate(ticks));
    }

    @Getter
    public static class CubeWrapper {
        private final Vector3f rotationPoint = new Vector3f();
        private final Vector3f prevRotationPoint = new Vector3f();
        private final Vector3f rotation = new Vector3f();
        private final Vector3f prevRotation = new Vector3f();


        public CubeWrapper(AnimationLayer.AnimatableCube box) {
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

}
