package net.dumbcode.dumblibrary.server.ecs.component.events;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;

@Getter
public class FeedingChangeEvent extends ComponentEvent {

    private final boolean isStarted;

    public FeedingChangeEvent(ComponentAccess componentAccess, boolean isStarted) {
        super(componentAccess);
        this.isStarted = isStarted;
    }
}
