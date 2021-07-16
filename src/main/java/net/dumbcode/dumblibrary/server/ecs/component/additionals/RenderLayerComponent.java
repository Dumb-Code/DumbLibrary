package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexConsumer;
import lombok.Value;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface RenderLayerComponent {
    void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<RenderLayer>> registry);


    @Value
    class Layer implements Consumer<BufferBuilder> {
        float red;
        float green;
        float blue;
        float alpha;
        ResourceLocation texture;

        @Override
        public void accept(BufferBuilder buffer) {
            RenderSystem.color4f(this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
            Minecraft.getInstance().textureManager.bind(this.getTexture());
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.vertex(0, 1, -2).uv(0, 1).color(255, 255, 255, 255).endVertex();
            buffer.vertex(1, 1, -2).uv(1, 1).color(255, 255, 255, 255).endVertex();
            buffer.vertex(1, 0, -2).uv(1, 0).color(255, 255, 255, 255).endVertex();
            buffer.vertex(0, 0, -2).uv(0, 0).color(255, 255, 255, 255).endVertex();
            Tessellator.getInstance().end();
        }
    }
}
