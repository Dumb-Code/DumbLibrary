package net.dumbcode.dumblibrary.server.animation.container;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimatableCubeEmpty;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;

public abstract class AnimationContainerEntry {

    protected TabulaModel modelCache;
    @Getter
    protected final AnimationLayer animationLayer;

    public AnimationContainerEntry(AnimationContainer container, Object source) {
        this.animationLayer = new AnimationLayer(Lists.newArrayList(), s -> AnimatableCubeEmpty.INSTANCE, container.createDataGetter(), source);
    }

    public void applyAnimations(float partialTicks, TabulaModel model) {
        model.resetAnimations();
        if(model != this.modelCache) {
            this.animationLayer.setFromModel(model);
            this.modelCache = model;
        }
        this.animationLayer.animate(partialTicks);
    }
}
