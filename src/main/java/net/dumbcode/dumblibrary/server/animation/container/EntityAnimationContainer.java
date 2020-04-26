package net.dumbcode.dumblibrary.server.animation.container;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimatableCubeEmpty;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;
import net.minecraft.entity.Entity;

public abstract class EntityAnimationContainer {

    protected TabulaModel modelCache;
    @Getter
    protected final AnimationLayer animationLayer;

    public EntityAnimationContainer(AnimationContainer container, Entity entity) {
        this.animationLayer = new AnimationLayer(entity, Lists.newArrayList(), s -> AnimatableCubeEmpty.INSTANCE, container.createDataGetter());
    }

    public void applyAnimations(float totalTicks, TabulaModel model) {
        if(model != this.modelCache) {
            this.animationLayer.setFromModel(model);
            this.modelCache = model;
        }
        this.animationLayer.animate(totalTicks);
    }
}
