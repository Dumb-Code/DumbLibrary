package net.dumbcode.dumblibrary.client.animation.objects;

import lombok.val;
import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;

import java.util.List;

/**
 * The wrapper for the {@link AnimationPass}. Used to controll multiple {@link AnimationPass}s
 * @param <T> the entity type this is used for
 */
public class AnimationPassWrapper<T extends EntityAnimatable> {
    private List<AnimationPass<T>> entityList;

    /**
     * @param entity the entity
     * @param model the model used
     * @param pairs a list of all animation passes to use
     */
    public AnimationPassWrapper(T entity, TabulaModel model, List<AnimationPass<T>> pairs) {
        this.entityList = pairs;
        //Iterate through all the animation passes, and call AnimationPass#init on them
        for (val animationPass : this.entityList) {
            animationPass.init(model, entity);
        }
    }

    /**
     * Perform the Animations on all the animation passes
     * @param entity the entity
     * @param limbSwing the limb swing
     * @param limbSwingAmount the limb swing amount
     * @param ticks the age of the entity in ticks
     */
    public void performAnimations(T entity, float limbSwing, float limbSwingAmount, float ticks) {
        for (val animationPass : this.entityList) {
            if(animationPass.shouldRun(entity)) {
                animationPass.performAnimations(entity, limbSwing, limbSwingAmount, ticks);
            }
        }
    }

}
