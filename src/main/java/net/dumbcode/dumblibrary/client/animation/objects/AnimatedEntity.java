package net.dumbcode.dumblibrary.client.animation.objects;

import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.minecraft.util.IStringSerializable;

//TODO: move to ecs
public interface AnimatedEntity<N extends IStringSerializable> {
    Animation<N> getAnimation();

    void setAnimation(Animation<N> animation);

    AnimationSystemInfo<N, ? extends AnimatedEntity> getInfo();
}
