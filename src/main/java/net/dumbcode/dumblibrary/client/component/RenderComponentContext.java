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

    //TODO: This is kinda strict configuration stuff. No ideas how to really expand it.
    // Maybe an array of ConfigurableLocation, with each index being a specif thing,
    // Like index 0 is texture, index 1 is file location ect ect.
    private final RenderLocationComponent.ConfigurableLocation texture;
    private final RenderLocationComponent.ConfigurableLocation fileLocation; //Optional

}
