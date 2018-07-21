package net.dumbcode.dumblibrary.client.animation.objects;


import net.dumbcode.dumblibrary.client.animation.AnimationInfo;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * The {@link ITabulaModelAnimator} used for this entity
 * @param <T> the entity type
 */
@SideOnly(Side.CLIENT)
public class EntityAnimator<T extends Entity & EntityAnimatable> implements ITabulaModelAnimator<T> {

    private final PoseHandler poseHandler;
    private final Animation defaultAnimation;
    private final Function<Animation, AnimationInfo> animationInfoGetter;
    private final PoseHandler.AnimationPassesFactory[] factories;

    protected EnumMap<GrowthStage, Map<T, AnimationPassWrapper<T>>> animationHandlers = new EnumMap<>(GrowthStage.class);

    /**
     * @param poseHandler The {@link PoseHandler}, needed for information about poses
     * @param defaultAnimation The default animation. Should be idle or somthing similar
     * @param animationInfoGetter a function to get the animation information from an animation
     * @param factories a list of {@link PoseHandler.AnimationPassesFactory} (Note these should be Object::new)
     */
    public EntityAnimator(PoseHandler poseHandler, Animation defaultAnimation,
                          Function<Animation, AnimationInfo> animationInfoGetter,
                          PoseHandler.AnimationPassesFactory... factories) {
        this.poseHandler = poseHandler;
        this.defaultAnimation = defaultAnimation;
        this.animationInfoGetter = animationInfoGetter;
        this.factories = factories;
    }

    /**
     * Get the {@link AnimationPassWrapper} linked with this entity. If there is none, add it
     * @param entity the entity
     * @param model the model
     * @param useInertialTweens whether inertial tweens should be used
     * @return the pass wrapper
     */
    @SuppressWarnings("rawtypes")
    private AnimationPassWrapper<T> getAnimationPassWrapper(T entity, TabulaModel model, boolean useInertialTweens) {
        GrowthStage growth = entity.getGrowthStage();
        return this.animationHandlers.computeIfAbsent(growth, g -> new WeakHashMap<>()).computeIfAbsent(entity, e -> this.poseHandler.createAnimationWrapper(e, model, defaultAnimation, animationInfoGetter, growth, useInertialTweens, factories));
    }

    @Override
    public final void setRotationAngles(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
        this.getAnimationPassWrapper(entity, model, true).performAnimations(entity, limbSwing, limbSwingAmount, ticks);
        this.performAnimations(model, entity, limbSwing, limbSwingAmount, ticks, rotationYaw, rotationPitch, scale);
    }

    /**
     * An extra method to perform more animations.
     * @param parModel The model
     * @param entity The entity
     * @param limbSwing the limb swing
     * @param limbSwingAmount the limb swing amount
     * @param ticks the ticks
     * @param rotationYaw the rotation yaw
     * @param rotationPitch the rotation pitch
     * @param scale the scale
     */
    protected void performAnimations(TabulaModel parModel, T entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
    }

}