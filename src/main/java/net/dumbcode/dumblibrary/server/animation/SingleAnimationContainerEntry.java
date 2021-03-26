package net.dumbcode.dumblibrary.server.animation;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.studio.animation.info.AnimationEntryData;

import java.util.UUID;

public class SingleAnimationContainerEntry extends AnimationContainerEntry {

    private UUID playingUUID;

    protected SingleAnimationContainerEntry(AnimationContainer container, Object source) {
        super(container, source);
    }

    public AnimationEntryData playAnimation(Animation animation) {
        if(this.playingUUID != null) {
            this.handler.markRemoved(this.playingUUID);
        }
        Pair<AnimationEntryData, UUID> pair = this.beginAnimation(animation);
        this.playingUUID = pair.getSecond();
        return pair.getFirst();
    }
}
