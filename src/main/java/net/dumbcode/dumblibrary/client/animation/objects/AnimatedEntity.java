package net.dumbcode.dumblibrary.client.animation.objects;

import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;

//TODO: move to ecs
public interface AnimatedEntity {
    Animation getAnimation();

    void setAnimation(Animation animation);

    AnimationSystemInfo getInfo();
}
