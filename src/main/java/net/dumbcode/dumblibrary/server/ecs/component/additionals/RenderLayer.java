package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.google.common.base.Suppliers;
import lombok.Getter;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public interface RenderLayer {
    void render(Tessellator tessellator, BufferBuilder buffer);

    default int getWidth() {
        return -1;
    }

    @Getter
    class DefaultTexture implements RenderLayer {
        private final Supplier<DefaultLayerData> dataSupplier;

        public DefaultTexture(ResourceLocation texture, float red, float green, float blue, float alpha) {
            this(Suppliers.ofInstance(new DefaultLayerData(texture, red, green, blue, alpha)));
        }

        public DefaultTexture(Supplier<DefaultLayerData> dataSupplier) {
            this.dataSupplier = dataSupplier;
        }

        @Override
        public int getWidth() {
            this.bind();
            return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        }

        @Override
        public void render(Tessellator tessellator, BufferBuilder buffer) {
            DefaultLayerData data = this.dataSupplier.get();
            if(data == null) {
                return;
            }
            this.bind();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
            buffer.vertex(0, 1, -2).color(data.red, data.green, data.blue, data.alpha).uv(0, 0).endVertex();
            buffer.vertex(1, 1, -2).color(data.red, data.green, data.blue, data.alpha).uv(1, 0).endVertex();
            buffer.vertex(1, 0, -2).color(data.red, data.green, data.blue, data.alpha).uv(1, 1).endVertex();
            buffer.vertex(0, 0, -2).color(data.red, data.green, data.blue, data.alpha).uv(0, 1).endVertex();
            tessellator.end();
        }

        private void bind() {
            DefaultLayerData data = this.dataSupplier.get();
            if(data != null) {
                Minecraft.getInstance().textureManager.bind(data.getTexture());
            }
        }
    }

    @Value
    class DefaultLayerData {
        ResourceLocation texture;

        float red;
        float green;
        float blue;
        float alpha;

        public DefaultLayerData(ResourceLocation texture) {
            this(texture, 1F, 1F, 1F, 1F);
        }

        public DefaultLayerData(ResourceLocation texture, float red, float green, float blue, float alpha) {
            this.texture = texture;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }


}
