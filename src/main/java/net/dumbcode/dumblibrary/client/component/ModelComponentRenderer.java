package net.dumbcode.dumblibrary.client.component;

import net.dumbcode.dumblibrary.client.FramebufferCache;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModelComponentRenderer extends RenderLiving<EntityLiving> implements RenderCallbackComponent.MainCallback {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private final Supplier<TabulaModel> modelSupplier;
    private final RenderLocationComponent.ConfigurableLocation texture;
    private final List<Supplier<RenderLayerComponent.Layer>> layerList;

    public ModelComponentRenderer(float shadowSize, Supplier<TabulaModel> modelSupplier, RenderLocationComponent.ConfigurableLocation texture, List<Supplier<RenderLayerComponent.Layer>> layerList) {
        super(MC.getRenderManager(), ModelMissing.INSTANCE, shadowSize);
        this.modelSupplier = modelSupplier;
        this.texture = texture;
        this.layerList = layerList;
    }

    Runnable preCallbacks;

    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
    }

    @Override
    public void invoke(RenderComponentContext context, Entity entity, double x, double y, double z, float entityYaw, float partialTicks, List<RenderCallbackComponent.SubCallback> preCallbacks, List<RenderCallbackComponent.SubCallback> postCallbacks) {

//            this.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
        TabulaModel model = this.modelSupplier.get();
        this.mainModel = model;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0F, 0F, 0F);
        if(!model.isRendered()) {
            model.renderBoxes(1/16F);
        }
        GlStateManager.popMatrix();

        this.preCallbacks = () -> {
            for (RenderCallbackComponent.SubCallback callback : preCallbacks) {
                callback.invoke(context, entity, x, y, z, entityYaw, partialTicks);
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

        if(entity instanceof EntityLiving) {
            this.doRender((EntityLiving) entity, x, y, z, entityYaw, partialTicks);
        } else {
            throw new NotImplementedException("Not implemented yet. Entity needs to be a subclass of EntityLivingBase");
        }

        for (RenderCallbackComponent.SubCallback callback : postCallbacks) {
            callback.invoke(context, entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Override
    protected void renderModel(EntityLiving entityLiving, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        Framebuffer frameBuffer = this.getFrameBuffer();
        frameBuffer.bindFramebuffer(true);
        this.renderAllLayers();

        MC.getFramebuffer().bindFramebuffer(true);
        frameBuffer.bindFramebufferTexture();
        this.mainModel.render(entityLiving, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    private void renderAllLayers() {
        GlStateManager.clearColor(0F, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.color(1f, 1f, 1f, 1f);

        MC.entityRenderer.disableLightmap();
        GlStateManager.disableLighting();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        int lastMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, 1, 0, 1, 0, 1000);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        for (Supplier<RenderLayerComponent.Layer> supplier : this.layerList) {
            RenderLayerComponent.Layer layer = supplier.get();
            GlStateManager.color(layer.getRed(), layer.getGreen(), layer.getBlue(), layer.getAlpha());
            MC.renderEngine.bindTexture(layer.getTexture());
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(0, 1, -2).tex(0, 1).color(255, 255, 255, 255).endVertex();
            buffer.pos(1, 1, -2).tex(1, 1).color(255, 255, 255, 255).endVertex();
            buffer.pos(1, 0, -2).tex(1, 0).color(255, 255, 255, 255).endVertex();
            buffer.pos(0, 0, -2).tex(0, 0).color(255, 255, 255, 255).endVertex();
            Tessellator.getInstance().draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(lastMode);
        GlStateManager.popMatrix();

        MC.entityRenderer.enableLightmap();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

    }

    private Framebuffer getFrameBuffer() {
        int width = this.mainModel.textureWidth;
        for (Supplier<RenderLayerComponent.Layer> supplier : this.layerList) {
            MC.renderEngine.bindTexture(supplier.get().getTexture());
            width = Math.max(width, GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH));
        }
        int height = (int) ((float) width * this.mainModel.textureHeight /  this.mainModel.textureWidth);
        return FramebufferCache.getFrameBuffer(width, height);
    }

    @Override
    protected void renderLayers(EntityLiving entityLiving, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
//        GlStateManager.enableNormalize();
//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        super.renderLayers(entityLiving, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);
//        GlStateManager.disableBlend();
//        GlStateManager.disableNormalize();
    }

    @Override
    protected void preRenderCallback(EntityLiving entityLiving, float partialTickTime) {
        this.preCallbacks.run();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLiving entity) {
        return this.texture.getLocation();
    }
}