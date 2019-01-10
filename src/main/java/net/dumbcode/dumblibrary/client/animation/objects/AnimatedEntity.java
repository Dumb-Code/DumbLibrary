package net.dumbcode.dumblibrary.client.animation.objects;

import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;

//TODO: move to ecs
public interface AnimatedEntity {
    Animation getAnimation();

    void setAnimation(Animation animation);

    DinosaurEntitySystemInfo getInfo();
}
