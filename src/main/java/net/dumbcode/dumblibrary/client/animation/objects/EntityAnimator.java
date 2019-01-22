package net.dumbcode.dumblibrary.client.animation.objects;


import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The {@link ITabulaModelAnimator} used for this entity
 * @param <T> the entity type
 */
@SideOnly(Side.CLIENT)
public class EntityAnimator<T extends EntityLiving & AnimatedEntity<N>, N extends IStringSerializable> implements ITabulaModelAnimator<T> {

    private final PoseHandler<T, N> poseHandler;
    private final Animation defaultAnimation;
    private final List<PoseHandler.AnimationLayerFactory<T, N>> factories;

    protected HashMap<N, Map<T, AnimationRunWrapper<T, N>>> animationHandlers;


    public EntityAnimator(PoseHandler<T, N> poseHandler) {
        this.poseHandler = poseHandler;
        this.animationHandlers = Maps.newHashMap();
        this.defaultAnimation = this.poseHandler.getInfo().defaultAnimation();
        this.factories = this.poseHandler.getInfo().createFactories();
    }

    /**
     * Get the {@link AnimationRunWrapper} linked with this entity. If there is none, add it
     * @param entity the entity
     * @param model the model
     * @param inertia whether inertia should be used
     * @return the pass wrapper
     */
    @SuppressWarnings("rawtypes")
    private AnimationRunWrapper<T, N> getAnimationPassWrapper(T entity, TabulaModel model, boolean inertia) {
        N growth = this.poseHandler.getInfo().getStageFromEntity(entity);
        return this.animationHandlers.computeIfAbsent(growth, g -> new WeakHashMap<>()).computeIfAbsent(entity, e -> this.poseHandler.createAnimationWrapper(e, model, defaultAnimation, growth, inertia, factories));
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