package net.dumbcode.dumblibrary.client.animation.objects;


import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.ilexiconn.llibrary.client.model.tabula.ITabulaModelAnimator;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The {@link ITabulaModelAnimator} used for this entity
 * @param <E> the entity type
 */
@SideOnly(Side.CLIENT)
public class EntityAnimator<E extends Entity, N extends IStringSerializable> implements ITabulaModelAnimator<E> {

    private final PoseHandler<E, N> poseHandler;
    private final List<PoseHandler.AnimationLayerFactory<E, N>> factories;

    protected HashMap<N, Map<E, AnimationRunWrapper<E, N>>> animationHandlers;


    public EntityAnimator(PoseHandler<E, N> poseHandler) {
        this.poseHandler = poseHandler;
        this.animationHandlers = Maps.newHashMap();
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
    private AnimationRunWrapper<E, N> getAnimationPassWrapper(E entity, TabulaModel model, boolean inertia) {
        N growth = this.poseHandler.getInfo().getStageFromEntity(entity);
        return this.animationHandlers.computeIfAbsent(growth, g -> new WeakHashMap<>()).computeIfAbsent(entity, e -> this.poseHandler.createAnimationWrapper(e, model, growth, inertia, factories));
    }

    @Override
    public final void setRotationAngles(TabulaModel model, E entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
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
    protected void performAnimations(TabulaModel parModel, E entity, float limbSwing, float limbSwingAmount, float ticks, float rotationYaw, float rotationPitch, float scale) {
    }

}