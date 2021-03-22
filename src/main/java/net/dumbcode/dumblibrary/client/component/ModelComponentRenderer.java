package net.dumbcode.dumblibrary.client.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.FramebufferCache;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModelComponentRenderer extends LivingRenderer<LivingEntity, EntityModel<LivingEntity>> implements RenderCallbackComponent.MainCallback {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final ResourceLocation DL_MODEL_TEX_LOCATION = new ResourceLocation(DumbLibrary.MODID, "dl_model_override_texture@@");
    private static final EditableTexture DL_MODEL_TEX = new EditableTexture();

    private final Supplier<TabulaModel> modelSupplier;
    private final RenderLocationComponent.ConfigurableLocation texture;
    private final List<Supplier<RenderLayerComponent.Layer>> layerList;


    public ModelComponentRenderer(float shadowSize, Supplier<TabulaModel> modelSupplier, RenderLocationComponent.ConfigurableLocation texture, List<Supplier<RenderLayerComponent.Layer>> layerList) {
        super(MC.getEntityRenderDispatcher(), ModelMissing.INSTANCE, shadowSize);
        this.modelSupplier = modelSupplier;
        this.texture = texture;
        this.layerList = layerList;
        Minecraft.getInstance().textureManager.register(DL_MODEL_TEX_LOCATION, DL_MODEL_TEX);
    }

    Consumer<MatrixStack> preCallbacks;

    @Override
    public void invoke(RenderComponentContext context, Entity entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int light, List<RenderCallbackComponent.SubCallback> preCallbacks, List<RenderCallbackComponent.SubCallback> postCallbacks) {
//            this.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);

        Framebuffer frameBuffer = this.getFrameBuffer();
        frameBuffer.bindWrite(true);
        this.renderAllLayers();
        MC.getMainRenderTarget().bindWrite(true);
        DL_MODEL_TEX.setId(frameBuffer.getColorTextureId());

        TabulaModel model = this.modelSupplier.get();
        this.model = (EntityModel<LivingEntity>)(Object)model;

//        GlStateManager.pushMatrix();
//        GlStateManager.scale(0F, 0F, 0F);
        if(!model.isRendered()) {
            model.renderBoxes(1/16F);
        }
//        GlStateManager.popMatrix();

        this.preCallbacks = ms -> {
            for (RenderCallbackComponent.SubCallback callback : preCallbacks) {
                callback.invoke(context, entity, entityYaw, partialTicks, ms, buffer, light);
            }
        };

        //TODO: need to do default model rendering, without all the stuff that RenderLivingBase does (death rotation, ect)
        // have our own implimentation of the rendering, that just focus on the model itsself.
        // we might not even have to set the model values
        // All we need to do is just rotate and translate correctly, then have a different component that would just
        // have a preRender callback that sets the model rotation stuff.
        // That component would handles all the changing values like yaw and pitch
        // We would also then have another component for say death-time
        // OR
        // An alternative to havin all these different components would be to have one component then have tuple
        // additions to allow/disallow certian modules. Other mods can just add their own component

        if(entity instanceof LivingEntity) {
            this.render((LivingEntity) entity, entityYaw, partialTicks, stack, buffer, light);
        } else {
            throw new NotImplementedException("Not implemented yet. Entity needs to be a subclass of EntityLivingBase");
        }

        for (RenderCallbackComponent.SubCallback callback : postCallbacks) {
            callback.invoke(context, entity, entityYaw, partialTicks, stack, buffer, light);
        }
    }

    @Override
    public void render(LivingEntity entity, float p_225623_2_, float partialRenderTick, MatrixStack stack, IRenderTypeBuffer buffer, int light) {
        super.render(entity, p_225623_2_, partialRenderTick, stack, buffer, light);
    }


    private void renderAllLayers() {
        RenderSystem.clearColor(0F, 0, 0, 0);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, true);
        RenderSystem.color4f(1f, 1f, 1f, 1f);

//        MC.entityRenderer.disableLightmap();
        RenderSystem.disableLighting();

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();

        int lastMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        RenderSystem.pushMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0, 1, 0, 1, 0, 1000);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();

        BufferBuilder buffer = Tessellator.getInstance().getBuilder();

        for (Supplier<RenderLayerComponent.Layer> supplier : this.layerList) {
            RenderLayerComponent.Layer layer = supplier.get();
            RenderSystem.color4f(layer.getRed(), layer.getGreen(), layer.getBlue(), layer.getAlpha());
            MC.textureManager.bind(layer.getTexture());
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.vertex(0, 1, -2).uv(0, 1).color(255, 255, 255, 255).endVertex();
            buffer.vertex(1, 1, -2).uv(1, 1).color(255, 255, 255, 255).endVertex();
            buffer.vertex(1, 0, -2).uv(1, 0).color(255, 255, 255, 255).endVertex();
            buffer.vertex(0, 0, -2).uv(0, 0).color(255, 255, 255, 255).endVertex();
            Tessellator.getInstance().end();
        }

        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(lastMode);
        RenderSystem.popMatrix();

//        MC.entityRenderer.enableLightmap();
        RenderSystem.enableLighting();
        RenderSystem.disableBlend();

    }

    private Framebuffer getFrameBuffer() {
        int width = this.model.texWidth;
        for (Supplier<RenderLayerComponent.Layer> supplier : this.layerList) {
            MC.textureManager.bind(supplier.get().getTexture());
            width = Math.max(width, GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH));
        }
        int height = (int) ((float) width * this.model.texWidth /  this.model.texHeight);
        return FramebufferCache.getFrameBuffer(width, height);
    }

    @Override
    protected void scale(LivingEntity p_225620_1_, MatrixStack stack, float p_225620_3_) {
        this.preCallbacks.accept(stack);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntity entity) {
        return DL_MODEL_TEX_LOCATION;
    }

    private static class EditableTexture extends Texture {

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void load(IResourceManager p_195413_1_) {

        }
    }
}