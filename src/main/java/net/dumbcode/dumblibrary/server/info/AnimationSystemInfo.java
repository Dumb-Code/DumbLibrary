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
import java.util.Collection;
import java.util.List;

public interface AnimationSystemInfo<E extends Entity> {

    Collection<String> allAnimationNames();

    EntityAnimator<E> createAnimator(ModelContainer<E> modelContainer);

    Animation defaultAnimation();

    Animation getAnimation(String animation);

    List<ModelContainer.AnimationLayerFactory<E>> createFactories();

    @Nonnull
    Animation getAnimation(E entity);

    void setAnimation(E entity, @Nonnull Animation animation);

    @SideOnly(Side.CLIENT)
    ModelContainer<E> getModelContainer(E entity);

    ResourceLocation getTexture(E entity);

    ResourceLocation identifier();

    AnimationRunWrapper<E> getOrCreateWrapper(E entity, ModelContainer<E> modelContainer, TabulaModel model, boolean inertia);

    void setPoseData(Animation animation, List<PoseData> poseData);

    List<PoseData> getPoseData(Animation animation);
}
