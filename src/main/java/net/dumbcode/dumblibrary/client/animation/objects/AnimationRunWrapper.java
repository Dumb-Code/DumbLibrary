package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;

/**
 * The wrapper for the {@link AnimationLayer}. Used to control multiple {@link AnimationLayer}s
 *
 * @param <T> the entity type this is used for
 */
public class AnimationRunWrapper<T extends AnimatedEntity> {
    @Getter
    private final T entity;
    private final Map<Animation, List<PoseData>> animations = Maps.newHashMap();
    private final List<AnimationLayer<T>> layers;

    /**
     * @param entity the entity
     * @param layers a list of all animation layers to use
     */
    public AnimationRunWrapper(T entity, List<AnimationLayer<T>> layers) {
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
    public void performAnimations(T entity, float limbSwing, float limbSwingAmount, float ticks) {
        this.layers.forEach(l -> l.animate(ticks));
    }

    @Getter
    public static class CubeWrapper {
        private final Vector3f position = new Vector3f();
        private final Vector3f prevPosition = new Vector3f();
        private final Vector3f rotation = new Vector3f();
        private final Vector3f prevRotation = new Vector3f();
    }

}
