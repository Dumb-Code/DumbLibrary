package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.dumbcode.dumblibrary.client.animation.objects.Animation;
import net.dumbcode.dumblibrary.client.animation.objects.AnimatedEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AnimationSystemInfo<N extends IStringSerializable, E extends EntityLiving & AnimatedEntity> {

    N defaultStage();
    List<N> allAcceptedStages();
    N[] allValues();
    Map<N, String> stageToModelMap();
    Collection<String> allAnimationNames();
    EntityAnimator<E, N> createAnimator(PoseHandler<E, N> poseHandler);
    Animation getAnimation(String animation);
    Animation defaultAnimation();
    PoseHandler.AnimationLayerFactory[] createFactories();

    ModelContainer<E, N> getModelContainer(E entity);
    N getStageFromEntity(E entity);
    ResourceLocation getTexture(E entity);

    ResourceLocation identifier();
}
