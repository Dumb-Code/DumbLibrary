package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface AnimationSystemInfo<N extends IStringSerializable, E extends Entity> {

    N defaultStage();
    List<N> allAcceptedStages();
    N[] allValues();
    Map<N, String> stageToModelMap();
    Collection<String> allAnimationNames();
    EntityAnimator<E, N> createAnimator(PoseHandler<E, N> poseHandler);
    Animation<N> getAnimation(String animation);
    Animation<N> defaultAnimation();
    List<PoseHandler.AnimationLayerFactory<E, N>> createFactories();

    @Nonnull Animation<N> getAnimation(E entity);
    void setAnimation(E entity, @Nonnull Animation<N> animation);

    ModelContainer<E, N> getModelContainer(E entity);
    N getStageFromEntity(E entity);
    ResourceLocation getTexture(E entity);

    ResourceLocation identifier();

    default AnimationRunWrapper<E, N> onWrapperCreated(AnimationRunWrapper<E, N> animationWrapper) {
        return animationWrapper;
    }
}
