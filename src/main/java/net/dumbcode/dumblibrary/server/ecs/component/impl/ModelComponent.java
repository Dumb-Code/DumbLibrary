package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.dumbcode.dumblibrary.client.model.ModelMissing;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent.ConfigurableLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.util.List;

//Client usage only
@Getter
public class ModelComponent implements RenderCallbackComponent, FinalizableComponent {

    private final ConfigurableLocation texture = new ConfigurableLocation(".png");
    private final ConfigurableLocation fileLocation = new ConfigurableLocation();

    @SideOnly(Side.CLIENT)
    private ModelBase modelCache = null;

    private float shadowsize;

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        renderCallbacks.add(new Render());
    }



    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setFloat("ShadowSize", this.shadowsize);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.shadowsize = compound.getFloat("ShadowSize");
    }

    @SideOnly(Side.CLIENT)
    private void resetModelCache() {
        this.modelCache = null;
    }

    @SideOnly(Side.CLIENT)
    public ModelBase getModelCache() {
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
    }

    //TODO: this class needs re-doing. It's very yucky at the moment
    @SideOnly(Side.CLIENT)
    private class Render extends RenderLivingBase<EntityLivingBase> implements MainCallback {
        public Render() {
            super(Minecraft.getMinecraft().getRenderManager(), ModelMissing.INSTANCE, ModelComponent.this.shadowsize);
        }

        Runnable preCallbacks;

        @Override
        public void invoke(RenderComponentContext context, Entity entity, double x, double y, double z, float entityYaw, float partialTicks, List<SubCallback> preCallbacks, List<SubCallback> postCallbacks) {
            this.shadowSize = ModelComponent.this.shadowsize;
            this.mainModel = ModelComponent.this.getModelCache();

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
        protected void preRenderCallback(EntityLivingBase entitylivingbaseIn, float partialTickTime) {
            this.preCallbacks.run();
        }


        @Nullable
        @Override
        protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
            return ModelComponent.this.texture.getLocation();
        }
    }
}
