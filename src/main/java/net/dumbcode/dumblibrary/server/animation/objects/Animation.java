package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Simple class to hold the animation data. This will most likely be removed later on when I re-do the animation system
 * @author Wyn Price
 */
@Data
public class Animation {
    @Accessors(fluent = true) private final boolean hold;
    @Accessors(fluent = true) private final boolean inertia;
    private final String identifier;

    public Animation(boolean hold, boolean inertia, String identifier) {
        this.hold = hold;
        this.inertia = inertia;
        this.identifier = identifier;
    }
}
