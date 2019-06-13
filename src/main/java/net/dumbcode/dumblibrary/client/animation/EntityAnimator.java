package net.dumbcode.dumblibrary.client.animation;


import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelAnimator;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * The {@link TabulaModelAnimator} used for this entity
 * @param <E> the entity type
 */
@SideOnly(Side.CLIENT)
public class EntityAnimator<E extends Entity> implements TabulaModelAnimator<E> {

    private final ModelContainer<E> modelContainer;
    private final List<ModelContainer.AnimationLayerFactory<E>> factories;


    public EntityAnimator(ModelContainer<E> poseHandler) {
        this.modelContainer = poseHandler;
        this.factories = this.modelContainer.getInfo().createFactories();
    }

    /**
     * Get the {@link AnimationRunWrapper} linked with this entity. If there is none, create it
     * @param entity the entity
     * @param model the model
     * @param inertia whether inertia should be used
     * @return the pass wrapper
     */
    @SuppressWarnings("rawtypes")
    private AnimationRunWrapper<E> getAnimationPassWrapper(E entity, TabulaModel model, boolean inertia) {
        return modelContainer.getInfo().getOrCreateWrapper(entity, this.modelContainer, model, inertia);
    }



    @Override
    public void setRotationAngles(TabulaModel model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, E entityIn) {
        this.getAnimationPassWrapper(entityIn, model, true).performAnimations(entityIn, limbSwing, limbSwingAmount, ageInTicks);
        this.performAnimations(model, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
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
        // Optional
    }


}