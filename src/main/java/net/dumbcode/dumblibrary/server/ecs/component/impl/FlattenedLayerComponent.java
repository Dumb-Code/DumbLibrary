package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderFlattenedLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayer;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.FlattenedLayerProperty;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FlattenedLayerComponent extends EntityComponent implements RenderLayerComponent {

    private final List<IndexedObject<FlattenedLayerProperty.Static>> staticLayers = new ArrayList<>();

    @Override
    public void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<RenderLayer>> registry) {
        List<IndexedObject<? extends FlattenedLayerProperty>> layerEntries = new ArrayList<>(this.staticLayers);
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderFlattenedLayerComponent) {
                ((RenderFlattenedLayerComponent) component).gatherComponents(entity, layerEntries::add);
            }
        }

        Optional<RenderLocationComponent.ConfigurableLocation> location = entity.get(EntityComponentTypes.MODEL).map(ModelComponent::getTexture);
        if(!location.isPresent()) {
            return;
        }
        RenderLocationComponent.ConfigurableLocation baseLocation = location.get();

        for (IndexedObject<? extends FlattenedLayerProperty> entry : layerEntries) {
            registry.accept(new IndexedObject<>(
                new RenderLayer.DefaultTexture(() -> {
                    String s = entry.getObject().currentValue();
                    return s == null ? null : new RenderLayer.DefaultLayerData(baseLocation.copy().addFileName(s, Integer.MAX_VALUE).getLocation());
                }), entry.getIndex()
            ));
        }

    }

    @Override
    public void serialize(PacketBuffer buf) {
        super.serialize(buf);
        buf.writeShort(this.staticLayers.size());
        for (IndexedObject<FlattenedLayerProperty.Static> layer : this.staticLayers) {
            IndexedObject.serializeByteBuf(buf, layer, aStatic -> buf.writeUtf(aStatic.getValue()));
        }
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        super.deserialize(buf);
        this.staticLayers.clear();

        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.staticLayers.add(IndexedObject.deserializeByteBuf(buf, () -> new FlattenedLayerProperty.Static(buf.readUtf())));
        }
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("layers",
            this.staticLayers.stream()
                .map(io -> IndexedObject.serializeNBT(io, aStatic -> StringNBT.valueOf(aStatic.getValue())))
                .collect(CollectorUtils.toNBTTagList())
        );
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.staticLayers.clear();
        StreamUtils.stream(compound.getList("layers", Constants.NBT.TAG_COMPOUND))
            .map(t -> IndexedObject.deserializeNBT((CompoundNBT)t, b -> new FlattenedLayerProperty.Static(b.getAsString())))
            .forEach(this.staticLayers::add);
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<FlattenedLayerComponent> {

        private final List<IndexedObject<FlattenedLayerProperty.Static>> staticLayers = new ArrayList<>();

        public Storage staticLayer(String layerName, float index) {
            this.staticLayers.add(new IndexedObject<>(new FlattenedLayerProperty.Static(layerName), index));
            return this;
        }

        @Override
        public void constructTo(FlattenedLayerComponent component) {
            component.staticLayers.addAll(this.staticLayers);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("layers",
                this.staticLayers.stream()
                    .map(io -> IndexedObject.serializeJson(io, aStatic -> new JsonPrimitive(aStatic.getValue())))
                    .collect(CollectorUtils.toJsonArray())
            );
        }

        @Override
        public void readJson(JsonObject json) {
            this.staticLayers.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "layers"))
                .map(e -> IndexedObject.deserializeJson(e.getAsJsonObject(), b -> new FlattenedLayerProperty.Static(b.getAsString())))
                .forEach(this.staticLayers::add);
        }
    }
}
