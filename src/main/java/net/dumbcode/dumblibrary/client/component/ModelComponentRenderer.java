package net.dumbcode.dumblibrary.client.component;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.FramebufferCache;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayer;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayer.DefaultTexture.TINT_SHADER;

public class ModelComponentRenderer extends LivingRenderer<LivingEntity, EntityModel<LivingEntity>> implements RenderCallbackComponent.MainCallback {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final ResourceLocation DL_MODEL_TEX_LOCATION = new ResourceLocation(DumbLibrary.MODID, "dl_model_override_texture");
    private static final EditableTexture DL_MODEL_TEX = new EditableTexture();

    private final Supplier<DCMModel> modelSupplier;
    private final RenderLocationComponent.ConfigurableLocation texture;
    private final List<RenderLayer> layerList;


    public ModelComponentRenderer(float shadowSize, Supplier<DCMModel> modelSupplier, RenderLocationComponent.ConfigurableLocation texture, List<RenderLayer> layerList) {
        super(MC.getEntityRenderDispatcher(), ModelMissing.getInstance(), shadowSize);
        this.modelSupplier = modelSupplier;
        this.texture = texture;
        this.layerList = layerList;
        Minecraft.getInstance().textureManager.register(DL_MODEL_TEX_LOCATION, DL_MODEL_TEX);
    }

    Consumer<GuiGraphics> preCallbacks;

    @Override
    public void invoke(RenderComponentContext context, Entity entity, float entityYaw, float partialTicks, GuiGraphics stack, IRenderTypeBuffer discarded_buffer, int light, List<RenderCallbackComponent.SubCallback> preCallbacks, List<RenderCallbackComponent.SubCallback> postCallbacks) {
//            this.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);

        IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tesselator.getInstance().getBuilder());

        DCMModel model = this.modelSupplier.get();
        this.model = (EntityModel<LivingEntity>)(Object)model;

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

        buffer.endBatch();

    }

    @Override
    public void render(LivingEntity entity, float p_225623_2_, float partialRenderTick, GuiGraphics stack, IRenderTypeBuffer buffer, int light) {
        super.render(entity, p_225623_2_, partialRenderTick, stack, buffer, light);
    }


    private void renderAllLayers() {
        RenderSystem.clearColor(0F, 0, 0, 0.0F);

        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.color4f(1f, 1f, 1f, 1f);

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0, 1, 1 ,0, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        RenderSystem.loadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        RenderSystem.enableTexture();
        RenderSystem.disableLighting();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableColorMaterial();
        RenderSystem.disableDepthTest();


        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();

        for (RenderLayer layer : this.layerList) {
            layer.render(tessellator, tessellator.getBuilder());
        }

        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.depthMask(true);

    }

    private Framebuffer getFrameBuffer() {
        int width = this.model.texWidth;
        for (RenderLayer supplier : this.layerList) {
            int i = supplier.getWidth();
            if(i != -1) {
                width = Math.max(width, i);
            }
        }
        int height = (int) ((float) width * this.model.texWidth /  this.model.texHeight);
        return FramebufferCache.getFrameBuffer(width, height);
    }

    @Override
    protected void scale(LivingEntity p_225620_1_, GuiGraphics stack, float p_225620_3_) {
        this.preCallbacks.accept(stack);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntity entity) {
        Framebuffer frameBuffer = this.getFrameBuffer();
        frameBuffer.bindWrite(true);
        this.renderAllLayers();
        MC.getMainRenderTarget().bindWrite(true);
        DL_MODEL_TEX.setId(frameBuffer.getColorTextureId());

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