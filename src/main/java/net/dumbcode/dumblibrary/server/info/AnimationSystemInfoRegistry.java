package net.dumbcode.dumblibrary.server.info;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.objects.AnimatedEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;


/**
 * Used client-side for syncing
 */
public class AnimationSystemInfoRegistry {
    public static Map<ResourceLocation, AnimationSystemInfo> NAMESPACE = Maps.newHashMap();

    public static void setAnimationToEntity(AnimatedEntity entity, ResourceLocation info, String animation) {
        AnimationSystemInfo asi = NAMESPACE.get(info);
        if(asi != null) {
            entity.setAnimation(asi.getAnimation(animation));
        }
    }
}
