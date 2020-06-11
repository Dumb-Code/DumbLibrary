package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.component.ModelComponentLayerRenderer;
import net.dumbcode.dumblibrary.client.component.ModelComponentRenderer;
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
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

//Client usage only
public class ModelComponent extends EntityComponent implements RenderCallbackComponent, FinalizableComponent {

    @Getter
    private final ConfigurableLocation texture = new ConfigurableLocation(".png");
    @Getter
    private final ConfigurableLocation fileLocation = new ConfigurableLocation();

    @SideOnly(Side.CLIENT)
    private TabulaModel modelCache;

    private float shadowSize;

    @SideOnly(Side.CLIENT)
    private ModelComponentRenderer renderer;

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

        if(entity instanceof Entity && ((Entity) entity).world != null && ((Entity) entity).world.isRemote) {
            this.createRenderer(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    private void createRenderer(ComponentAccess entity) {
        if(this.renderer == null) {
            this.renderer = new ModelComponentRenderer(this.shadowSize, this::getModelCache, this.texture);
        }
        this.renderer.clearLayers();
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderLayerComponent) {
                ((RenderLayerComponent) component).gatherLayers(entity, rc -> this.renderer.addLayer(new ModelComponentLayerRenderer(rc, this.renderer::getRenderID)));
            }
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<ModelComponent> {

        private float shadowSize;

        @Override
        public void constructTo(ModelComponent component) {
            component.shadowSize = this.shadowSize;
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
