package net.dumbcode.dumblibrary.server.ecs.component.additionals;

import com.google.common.base.Suppliers;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.function.Supplier;

public interface RenderLayer {
    void render(Tessellator tessellator, BufferBuilder buffer);

    default int getWidth() {
        return -1;
    }

    @Getter
    class DefaultTexture implements RenderLayer {
        public static ShaderInstance TINT_SHADER;
        private final Supplier<DefaultLayerData> dataSupplier;

        public DefaultTexture(ResourceLocation texture, float red, float green, float blue, float alpha) {
            this(Suppliers.ofInstance(new DefaultLayerData(texture, red, green, blue, alpha)));
        }

        public DefaultTexture(Supplier<DefaultLayerData> dataSupplier) {
            this.dataSupplier = dataSupplier;
        }

        @Override
        public int getWidth() {
            this.bind(this.dataSupplier.get());
            return GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        }

        @Override
        public void render(Tessellator tessellator, BufferBuilder buffer) {
            DefaultLayerData data = this.dataSupplier.get();
            if(data == null) {
                return;
            }
            this.bind(data);
            if (this.startShader(data)) {
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.vertex(0, 1, -2).uv(0, 1).endVertex();
                buffer.vertex(1, 1, -2).uv(1, 1).endVertex();
                buffer.vertex(1, 0, -2).uv(1, 0).endVertex();
                buffer.vertex(0, 0, -2).uv(0, 0).endVertex();
                tessellator.end();
                this.endShader();
            }

        }

        private void bind(DefaultLayerData data) {
            if(data != null) {
                Minecraft.getInstance().textureManager.bind(data.getTexture());
            }
        }

        private boolean startShader(DefaultLayerData data) {
            if(TINT_SHADER == null) {
                try {
                    TINT_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), DumbLibrary.MODID + ":tint");
                } catch (IOException e) {
                    DumbLibrary.getLogger().error("Unable to load flip shader", e);
                }
            }
            if(TINT_SHADER != null) {
                Minecraft.getInstance().textureManager.bind(data.getTexture());
                Texture texture = Minecraft.getInstance().textureManager.getTexture(data.getTexture());
                if(texture != null) {
                    TINT_SHADER.setSampler("sampler", texture::getId);
                    TINT_SHADER.safeGetUniform("colour").set(data.red, data.green, data.blue, data.alpha);
                    TINT_SHADER.apply();
                }
                return true;
            }
            return false;
        }

        private void endShader() {
            if(TINT_SHADER != null) {
                TINT_SHADER.clear();
            }
        }

        public static void resetShader() {
            if(TINT_SHADER != null) {
                TINT_SHADER.close();
            }
            TINT_SHADER = null;
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
