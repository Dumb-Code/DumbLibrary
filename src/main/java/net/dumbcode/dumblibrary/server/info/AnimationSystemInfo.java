package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.EntityAnimator;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.animation.objects.PoseData;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

//todo: try and phase this out?
public interface AnimationSystemInfo<E extends Entity> {

    EntityAnimator<E> createAnimator(ModelContainer<E> modelContainer);

    List<ModelContainer.AnimationLayerFactory<E>> createFactories();

    @Nonnull
    Animation getAnimation(E entity);

    @SideOnly(Side.CLIENT)
    ModelContainer<E> getModelContainer(E entity);

    ResourceLocation getTexture(E entity);

    AnimationRunWrapper<E> getOrCreateWrapper(E entity, ModelContainer<E> modelContainer, TabulaModel model, boolean inertia);

    void setPoseData(Animation animation, List<PoseData> poseData);

    List<PoseData> getPoseData(Animation animation);
}
