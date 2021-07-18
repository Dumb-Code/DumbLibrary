package net.dumbcode.dumblibrary.client.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RenderComponentContext {
    private final List<RenderCallbackComponent.SubCallback> preRenderCallbacks;
    private final List<RenderCallbackComponent.MainCallback> renderCallbacks;
    private final List<RenderCallbackComponent.SubCallback> postRenderCallback;
}
