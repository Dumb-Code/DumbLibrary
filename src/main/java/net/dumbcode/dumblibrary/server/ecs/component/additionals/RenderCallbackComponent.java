package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public interface RenderCallbackComponent {

    @OnlyIn(Dist.CLIENT)
    void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback);

    interface SubCallback {
        void invoke(RenderComponentContext context, Entity entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light);
    }

    interface MainCallback {
        void invoke(RenderComponentContext context, Entity entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light, List<SubCallback> preCallbacks, List<SubCallback> postCallbacks);
    }
}
