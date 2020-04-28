package net.dumbcode.dumblibrary.server.animation.container;

import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.objects.Animation;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationWrap;

import java.util.Arrays;

public class ChannelAnimationContainerEntry extends AnimationContainerEntry {

    private final AnimationWrap[] channels;

    public ChannelAnimationContainerEntry(AnimationContainer container, Object source, int channels) {
        super(container, source);
        this.channels = new AnimationWrap[channels];
    }

    public boolean isChannelActive(int channel) {
        return this.channels[channel] != null;
    }

    public void playAnimation(Animation animation, int channel) {
        this.playAnimation(new AnimationEntry(animation), channel);
    }

    public void playAnimation(AnimationEntry entry, int channel) {
        if(channel >= 0) {
            AnimationWrap current = this.channels[channel];
            if(current != null) {
                this.animationLayer.removeAnimation(current);
            }
        }
        AnimationWrap wrap = this.animationLayer.create(entry);
        this.animationLayer.addAnimation(wrap);
        if(channel >= 0) {
            this.channels[channel] = wrap;
        }
    }

    public void stopAnimation(int channel) {
        AnimationWrap wrap = this.channels[channel];
        if(wrap != null) {
            this.animationLayer.removeAnimation(wrap);
            this.channels[channel] = null;
        }
    }

    public void stopAll() {
        for (AnimationWrap wrap : this.channels) {
            if(wrap != null) {
                this.animationLayer.removeAnimation(wrap);
            }
        }
        Arrays.fill(this.channels, null);

    }

}
