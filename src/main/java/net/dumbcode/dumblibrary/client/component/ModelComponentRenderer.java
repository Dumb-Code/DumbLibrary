package net.dumbcode.dumblibrary.client.component;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.ModelComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ModelComponentRenderer extends RenderLivingBase<EntityLivingBase> implements RenderCallbackComponent.MainCallback {

    @Getter
    private final int renderID;
    private final Supplier<TabulaModel> modelSupplier;
    private final RenderLocationComponent.ConfigurableLocation texture;

    public ModelComponentRenderer(float shadowSize, Supplier<TabulaModel> modelSupplier, RenderLocationComponent.ConfigurableLocation texture) {
        super(Minecraft.getMinecraft().getRenderManager(), ModelMissing.INSTANCE, shadowSize);
        this.renderID = GlStateManager.glGenLists(1);
        this.modelSupplier = modelSupplier;
        this.texture = texture;
    }

    public void clearLayers() {
        this.layerRenderers.clear();
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

        if(entity instanceof EntityLivingBase) {
            this.doRender((EntityLivingBase) entity, x, y, z, entityYaw, partialTicks);
        } else {
            throw new NotImplementedException("Not implemented yet. Entity needs to be a subclass of EntityLivingBase");
        }

        for (RenderCallbackComponent.SubCallback callback : postCallbacks) {
            callback.invoke(context, entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Override
    protected void renderModel(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        GlStateManager.glNewList(this.renderID, GL11.GL_COMPILE);
        this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        GlStateManager.glEndList();
    }

    @Override
    protected void renderLayers(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
        GlStateManager.enableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        super.renderLayers(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);
        GlStateManager.disableBlend();
        GlStateManager.disableNormalize();
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entitylivingbaseIn, float partialTickTime) {
        this.preCallbacks.run();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
        return this.texture.getLocation();
    }
}