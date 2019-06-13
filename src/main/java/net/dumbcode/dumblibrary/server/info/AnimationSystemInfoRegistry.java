package net.dumbcode.dumblibrary.server.info;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;


/**
 * Used client-side for syncing
 */
public class AnimationSystemInfoRegistry {
    public static final Map<ResourceLocation, AnimationSystemInfo> NAMESPACE = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static void setAnimationToEntity(Entity entity, ResourceLocation info, String animationName) {
        AnimationSystemInfo asi = NAMESPACE.get(info);
        if (asi != null) {
            Animation animation = asi.getAnimation(animationName);
            if (animation != null) {
                asi.setAnimation(entity, animation);
            }
        }
    }
}
