package net.dumbcode.dumblibrary.server.animation.container;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;
import net.minecraft.entity.Entity;

public class SingleAnimationEntityContainer extends EntityAnimationContainer {

    private AnimationWrap wrap;

    public SingleAnimationEntityContainer(AnimationContainer container, Entity entity) {
        super(container, entity);
    }


    public void playAnimation(Animation animation) {
        this.playAnimation(new AnimationEntry(animation));
    }

    public void playAnimation(AnimationEntry entry) {
        this.animationLayer.addAnimation(this.wrap = this.animationLayer.create(entry));
    }

    public void stopAnimation() {
        this.animationLayer.removeAnimation(this.wrap);
    }


}
