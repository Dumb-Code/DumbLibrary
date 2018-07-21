package net.dumbcode.dumblibrary.server.entity;

import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;

/**
 * Markes the entity as animatable. This is used as an interface to get animation infomation about the entity
 */
public interface EntityAnimatable extends IAnimatedEntity {

    GrowthStage getGrowthStage();
}
