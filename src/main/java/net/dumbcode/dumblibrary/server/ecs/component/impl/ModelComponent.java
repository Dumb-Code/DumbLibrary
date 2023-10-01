package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.component.ModelComponentRenderer;
import net.dumbcode.dumblibrary.client.component.RenderComponentContext;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.*;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent.ConfigurableLocation;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

//Client usage only
public class ModelComponent extends EntityComponent implements RenderCallbackComponent, FinalizableComponent {

    @Getter
    private final ConfigurableLocation texture = new ConfigurableLocation(".png");
    @Getter
    private final ConfigurableLocation fileLocation = new ConfigurableLocation();

    @OnlyIn(Dist.CLIENT)
    private DCMModel modelCache;

    private float shadowSize;

    @OnlyIn(Dist.CLIENT)
    private ModelComponentRenderer renderer;

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        renderCallbacks.add(
            (context, entity, entityYaw, partialTicks, stack, buffer, light, preCallbacks, postCallbacks) ->
                this.renderer.invoke(context, entity, entityYaw, partialTicks, stack, buffer, light, preCallbacks, postCallbacks)
        );
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putFloat("ShadowSize", this.shadowSize);
        return super.serialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeFloat(this.shadowSize);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.shadowSize = compound.getFloat("ShadowSize");
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.shadowSize = buf.readFloat();
    }


    @OnlyIn(Dist.CLIENT)
    public DCMModel getModelCache() {
        if(this.modelCache == null) {
            this.modelCache = DCMUtils.getModel(this.fileLocation.getLocation());
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

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof ModelSetComponent) {
                ((ModelSetComponent) component).onModelSet(this);
            }
        }

        if(entity instanceof Entity && ((Entity) entity).level != null && ((Entity) entity).level.isClientSide) {
            this.createRenderer(entity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void createRenderer(ComponentAccess entity) {
        if(this.renderer == null) {
            List<IndexedObject<RenderLayer>> layerList = new ArrayList<>();
            for (EntityComponent component : entity.getAllComponents()) {
                if(component instanceof RenderLayerComponent) {
                    ((RenderLayerComponent) component).gatherLayers(entity, layerList::add);
                }
            }
            this.renderer = new ModelComponentRenderer(this.shadowSize, this::getModelCache, this.texture, IndexedObject.sortIndex(layerList));
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
            this.shadowSize = JSONUtils.getAsFloat(json, "shadow_size");
        }
    }
}
