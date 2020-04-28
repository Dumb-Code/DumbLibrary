package net.dumbcode.dumblibrary.server.animation.container;

import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;

public class SingleAnimationContainerEntry extends AnimationContainerEntry {

    private AnimationWrap wrap;

    public SingleAnimationContainerEntry(AnimationContainer container, Object source) {
        super(container, source);
    }

    public boolean isPlaying() {
        return this.wrap != null && !this.wrap.isInvalidated();
    }

    public void playAnimation(Animation animation) {
        this.playAnimation(animation.createEntry());
    }

    public void playAnimation(AnimationEntry entry) {
        this.stopAnimation();
        this.animationLayer.addAnimation(this.wrap = this.animationLayer.create(entry));
    }

    public void stopAnimation() {
        if(this.wrap != null) {
            this.animationLayer.removeAnimation(this.wrap);
            this.wrap = null;
        }
    }


}
