package net.dumbcode.dumblibrary.server.info;

import net.dumbcode.dumblibrary.client.animation.AnimationInfo;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface AnimationSystemInfo<N extends IStringSerializable, E extends EntityLiving & IAnimatedEntity> {

    Class<N> enumClazz();
    N defaultStage();
    List<N> allAcceptedStages();
    N[] allValues();
    Map<N, String> stageToModelMap();
    Collection<String> allAnimationNames();
    EntityAnimator createAnimator(PoseHandler poseHandler, Animation defaultAnimation,
                                  Function<Animation, AnimationInfo> animationInfoGetter,
                                  PoseHandler.AnimationPassesFactory... factories);
    Animation getAnimation(String animation);
    Animation defaultAnimation();
    AnimationInfo getAnimationInfo(Animation animation);
    PoseHandler.AnimationPassesFactory[] createFactories();

    ModelContainer<N> getModelContainer(E entity);
    N getStageFromEntity(E entity);
    ResourceLocation getTexture(E entity);
}
