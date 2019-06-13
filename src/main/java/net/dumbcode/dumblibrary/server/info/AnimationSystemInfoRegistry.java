package net.dumbcode.dumblibrary.server.info;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;


/**
 * Used client-side for syncing
 */
public enum AnimationSystemInfoRegistry {
    INSTANCE;

    public final Map<ResourceLocation, AnimationSystemInfo> namespace = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public void setAnimationToEntity(Entity entity, ResourceLocation info, String animationName) {
        AnimationSystemInfo asi = namespace.get(info);
        if (asi != null) {
            Animation animation = asi.getAnimation(animationName);
            if (animation != null) {
                asi.setAnimation(entity, animation);
            }
        }
    }
}
