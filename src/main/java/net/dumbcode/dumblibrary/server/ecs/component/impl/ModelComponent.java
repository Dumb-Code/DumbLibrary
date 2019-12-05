package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent.ConfigurableLocation;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

//Client usage only
@Getter
public class ModelComponent extends EntityComponent implements RenderCallbackComponent, FinalizableComponent {

    private final ConfigurableLocation texture = new ConfigurableLocation(".png");
    private final ConfigurableLocation fileLocation = new ConfigurableLocation();

    @SideOnly(Side.CLIENT)
    private TabulaModel modelCache = null;

    private float shadowSize;

    @SideOnly(Side.CLIENT)
    private Render renderer;

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        renderCallbacks.add(this.renderer);
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setFloat("ShadowSize", this.shadowSize);
        return super.serialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeFloat(this.shadowSize);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.shadowSize = compound.getFloat("ShadowSize");
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.shadowSize = buf.readFloat();
    }

    @SideOnly(Side.CLIENT)
    private void resetModelCache() {
        this.modelCache = null;
    }

    @SideOnly(Side.CLIENT)
    public TabulaModel getModelCache() {
        if(this.modelCache == null) {
            this.modelCache = TabulaUtils.getModel(this.fileLocation.getLocation());
        }
        return this.modelCache;
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        this.texture.reset();
        this.fileLocation.reset();

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderLocationComponent) {
                ((RenderLocationComponent) component).editLocations(this.texture, this.fileLocation);
            }
        }

        if(entity instanceof Entity && ((Entity) entity).world.isRemote) {
            SidedExecutor.runClient(() -> () -> {
                if(this.renderer == null) {
                    this.renderer = new Render();
                }
                this.renderer.clearLayers();
                for (EntityComponent component : entity.getAllComponents()) {
                    if(component instanceof RenderLayerComponent) {
                        ((RenderLayerComponent) component).gatherLayers(rc -> this.renderer.addLayer(new Layer(rc)));
                    }
                }
            });
        }
    }

    //TODO: this class needs re-doing. It's very yucky at the moment
    @SideOnly(Side.CLIENT)
    private class Render extends RenderLivingBase<EntityLivingBase> implements MainCallback {

        private final int renderID;

        public Render() {
            super(Minecraft.getMinecraft().getRenderManager(), ModelMissing.INSTANCE, ModelComponent.this.shadowSize);
            this.renderID = GlStateManager.glGenLists(1);
        }

        private void clearLayers() {
            this.layerRenderers.clear();
        }

        Runnable preCallbacks;

        @Override
        public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
            super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
        }

        @Override
        public void invoke(RenderComponentContext context, Entity entity, double x, double y, double z, float entityYaw, float partialTicks, List<SubCallback> preCallbacks, List<SubCallback> postCallbacks) {
            this.shadowSize = ModelComponent.this.shadowSize;

//            this.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
            TabulaModel model = ModelComponent.this.getModelCache();
            this.mainModel = model;

            GlStateManager.pushMatrix();
            GlStateManager.scale(0F, 0F, 0F);
            if(!model.isRendered()) {
                model.renderBoxes(1/16F);
            }
            GlStateManager.popMatrix();

            this.preCallbacks = () -> {
                for (SubCallback callback : preCallbacks) {
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

            for (SubCallback callback : postCallbacks) {
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
            return ModelComponent.this.texture.getLocation();
        }
    }

    @RequiredArgsConstructor
    private class Layer implements LayerRenderer {

        private final Consumer<Runnable> callback;

        @Override
        public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.callback.accept(() -> GlStateManager.callList(ModelComponent.this.renderer.renderID));
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<ModelComponent> {

        private float shadowSize;

        @Override
        public ModelComponent constructTo(ModelComponent component) {
            component.shadowSize = this.shadowSize;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("shadow_size", this.shadowSize);
        }

        @Override
        public void readJson(JsonObject json) {
            this.shadowSize = JsonUtils.getFloat(json, "shadow_size");
        }
    }
}
