package net.dumbcode.dumblibrary.server.entity.component.additionals;

import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface RenderCallbackComponent extends EntityComponent {

    @SideOnly(Side.CLIENT)
    void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback);

    interface SubCallback {
        void invoke(RenderComponentContext context, Entity entity, double x, double y, double z, float entityYaw, float partialTicks);
    }

    interface MainCallback {
        void invoke(RenderComponentContext context, Entity entity, double x, double y, double z, float entityYaw, float partialTicks, List<SubCallback> preCallbacks, List<SubCallback> postCallbacks);
    }
}