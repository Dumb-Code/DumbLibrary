package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;

/**
 * The wrapper for the {@link AnimationLayer}. Used to control multiple {@link AnimationLayer}s
 *
 * @param <E> the entity type this is used for
 */
public class AnimationRunWrapper<E extends Entity, N extends IStringSerializable> {
    @Getter
    private final E entity;
    private final Map<Animation, List<PoseData>> animations = Maps.newHashMap();
    private final List<AnimationLayer<E, N>> layers;

    /**
     * @param entity the entity
     * @param layers a list of all animation layers to use
     */
    public AnimationRunWrapper(E entity, List<AnimationLayer<E, N>> layers) {
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
        private final Vector3f position = new Vector3f();
        private final Vector3f prevPosition = new Vector3f();
        private final Vector3f rotation = new Vector3f();
        private final Vector3f prevRotation = new Vector3f();
        
        public CubeWrapper(AdvancedModelRenderer box) {
            this.position.x = this.prevPosition.x = box.defaultPositionX;
            this.position.y = this.prevPosition.y = box.defaultPositionY;
            this.position.z = this.prevPosition.z = box.defaultPositionZ;

            this.rotation.x = this.prevRotation.x = box.defaultRotationX;
            this.rotation.y = this.prevRotation.y = box.defaultRotationY;
            this.rotation.z = this.prevRotation.z = box.defaultRotationZ;

        }
    }

}
