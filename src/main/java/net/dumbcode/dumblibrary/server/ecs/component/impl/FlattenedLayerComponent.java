package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderFlattenedLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.FlattenedLayerProperty;
import net.dumbcode.dumblibrary.server.utils.BuilderNode;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlattenedLayerComponent extends EntityComponent implements RenderLayerComponent {

    private float index;
    private final List<IndexedObject<FlattenedLayerProperty.Static>> staticLayers = new ArrayList<>();

    @Override
    public void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<Supplier<Layer>>> registry) {
        List<IndexedObject<FlattenedLayerProperty>> layerEntries = new ArrayList<>();
        for (IndexedObject<FlattenedLayerProperty.Static> layer : this.staticLayers) {
            layerEntries.add(new IndexedObject<>(layer.getObject(), layer.getIndex()));
        }
        Optional<RenderLocationComponent.ConfigurableLocation> location = entity.get(EntityComponentTypes.MODEL).map(ModelComponent::getTexture);

        if(!location.isPresent()) {
            return;
        }

        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof RenderFlattenedLayerComponent) {
                ((RenderFlattenedLayerComponent) component).gatherComponents(entity, layerEntries::add);
            }
        }

        //With the layers, [belly (1 texture), eyes (2 textures; blink & open), patterns (1 texture)]:
        //
        //                        belly
        //                          |
        //                          |
        //            -----------------------------
        //           /                             \
        //          /                               \
        //       blink                             open
        //         |                                 |
        //         |                                 |
        //      patterns                          patterns
        //         |                                 |
        //         |                                 |
        //   compiled_texture                 compiled_texture
        //
        //
        //When eyes -> blink, compiled_texture  = [belly, blink, patterns]
        //When eyes -> open,  compiled_texture  = [belly, open, patterns]
        //
        //These textures are compiled beforehand, and stored as strings. (Even though they're resource locations)

        if(!layerEntries.isEmpty()) {
            List<FlattenedLayerProperty> sortedByIndex = IndexedObject.sortIndex(layerEntries);

            BuilderNode.Entry<String> root = new BuilderNode.Entry<>(null);
            List<BuilderNode.Entry<String>> currentEntries = Lists.newArrayList(root);
            for (FlattenedLayerProperty property : sortedByIndex) {
                Set<String> allValues = property.allValues();
                List<BuilderNode.Entry<String>> newValues = new ArrayList<>();
                for (String value : allValues) {
                    for (BuilderNode.Entry<String> entry : currentEntries) {
                        BuilderNode.Entry<String> newEntry = new BuilderNode.Entry<>(value);
                        entry.getChildren().add(newEntry);
                        newValues.add(newEntry);
                    }
                }
                currentEntries.clear();
                currentEntries.addAll(newValues);
            }
            this.generateTexture(location.get(), new ArrayDeque<>(), root);

            registry.accept(new IndexedObject<>(() -> {
                BuilderNode.Entry<String> element = root;
                for (FlattenedLayerProperty sorted : sortedByIndex) {
                    String currentValue = sorted.currentValue();
                    element = element.getChildren().stream()
                        .filter(e -> e.getElement().equals(currentValue))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Don't know how to handle type: " + currentValue + " for specified values " + sorted.allValues()));
                }
                if(element.getChildren().size() != 1) {
                    throw new IllegalArgumentException("Error whilst traversing tree, reached destination: " + element.getChildren());
                }
                return new Layer(1F, 1F, 1F, 1F, new ResourceLocation(element.getChildren().get(0).getElement()));
            }, this.index));

        }
    }

    private void generateTexture(RenderLocationComponent.ConfigurableLocation baseLocation, Deque<String> currentStringStack, BuilderNode.Entry<String> currentNode) {
        if(currentNode.getChildren().isEmpty()) { //End of tree, time to generate texture.
            String location = "missingno";
            if(baseLocation != null) {
                location = TextureUtils.generateMultipleTexture(currentStringStack.stream().map(s -> baseLocation.copy().addFileName(s, Integer.MAX_VALUE).getLocation()).toArray(ResourceLocation[]::new)).toString();
            }
            currentNode.getChildren().add(new BuilderNode.Entry<>(location));
        } else {
            for (BuilderNode.Entry<String> child : currentNode.getChildren()) {
                currentStringStack.push(child.getElement());
                this.generateTexture(baseLocation, currentStringStack, child);
                currentStringStack.pop();
            }
        }
    }

    @Override
    public void serialize(PacketBuffer buf) {
        super.serialize(buf);
        buf.writeShort(this.staticLayers.size());
        for (IndexedObject<FlattenedLayerProperty.Static> layer : this.staticLayers) {
            IndexedObject.serializeByteBuf(buf, layer, aStatic -> buf.writeUtf(aStatic.getValue()));
        }
        buf.writeFloat(this.index);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        super.deserialize(buf);
        this.staticLayers.clear();

        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.staticLayers.add(IndexedObject.deserializeByteBuf(buf, () -> new FlattenedLayerProperty.Static(buf.readUtf())));
        }
        this.index = buf.readFloat();
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("layers",
            this.staticLayers.stream()
                .map(io -> IndexedObject.serializeNBT(io, aStatic -> StringNBT.valueOf(aStatic.getValue())))
                .collect(CollectorUtils.toNBTTagList())
        );
        compound.putFloat("Index", this.index);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.staticLayers.clear();
        StreamUtils.stream(compound.getList("layers", Constants.NBT.TAG_COMPOUND))
            .map(t -> IndexedObject.deserializeNBT((CompoundNBT)t, b -> new FlattenedLayerProperty.Static(b.getAsString())))
            .forEach(this.staticLayers::add);
        this.index = compound.getFloat("Index");
        super.deserialize(compound);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<FlattenedLayerComponent> {

        private float index;
        private final List<IndexedObject<FlattenedLayerProperty.Static>> staticLayers = new ArrayList<>();

        public Storage staticLayer(String layerName, float index) {
            this.staticLayers.add(new IndexedObject<>(new FlattenedLayerProperty.Static(layerName), index));
            return this;
        }

        @Override
        public void constructTo(FlattenedLayerComponent component) {
            component.staticLayers.addAll(this.staticLayers);
            component.index = this.index;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("layers",
                this.staticLayers.stream()
                    .map(io -> IndexedObject.serializeJson(io, aStatic -> new JsonPrimitive(aStatic.getValue())))
                    .collect(CollectorUtils.toJsonArray())
            );
            json.addProperty("index", this.index);
        }

        @Override
        public void readJson(JsonObject json) {
            this.staticLayers.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "layers"))
                .map(e -> IndexedObject.deserializeJson(e.getAsJsonObject(), b -> new FlattenedLayerProperty.Static(b.getAsString())))
                .forEach(this.staticLayers::add);
            this.index = JSONUtils.getAsFloat(json, "index");
        }
    }
}
