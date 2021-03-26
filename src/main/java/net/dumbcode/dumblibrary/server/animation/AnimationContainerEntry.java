package net.dumbcode.dumblibrary.server.animation;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.studio.animation.info.AnimationEntryData;
import net.dumbcode.studio.animation.info.AnimationInfo;
import net.dumbcode.studio.animation.instance.ModelAnimationHandler;

import java.util.UUID;

public abstract class AnimationContainerEntry {
    private final AnimationContainer container;
    protected final ModelAnimationHandler handler;

    protected AnimationContainerEntry(AnimationContainer container, Object source) {
        this.container = container;
        this.handler = new ModelAnimationHandler(source);
    }

    public void applyAnimations(DCMModel model, float partialTicks) {
        this.handler.animate(model.getAllCubes(), partialTicks);
    }

    protected Pair<AnimationEntryData, UUID> beginAnimation(Animation animation) {
        AnimationEntryData data = this.container.getInfo(animation).data();
        return Pair.of(data, this.handler.startAnimation(data));
    }


}
