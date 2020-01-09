package net.dumbcode.dumblibrary.server.ecs.component.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@RequiredArgsConstructor
public class ComponentEvent extends Event {
    private final ComponentAccess componentAccess;
}
