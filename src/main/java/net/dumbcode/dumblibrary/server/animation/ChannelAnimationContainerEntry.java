package net.dumbcode.dumblibrary.server.animation;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.studio.animation.info.AnimationEntryData;

import java.util.UUID;

public class ChannelAnimationContainerEntry extends AnimationContainerEntry {

    private final UUID[] channels;

    protected ChannelAnimationContainerEntry(AnimationContainer container, Object source, int channels) {
        super(container, source);
        this.channels = new UUID[channels];
    }

    public AnimationEntryData playAnimation(Animation animation, int channel) {
        UUID playingUUID = this.channels[channel];
        if(playingUUID != null) {
            this.handler.markRemoved(playingUUID);
        }
        Pair<AnimationEntryData, UUID> pair = this.beginAnimation(animation);
        this.channels[channel] = pair.getSecond();
        return pair.getFirst();
    }
}
