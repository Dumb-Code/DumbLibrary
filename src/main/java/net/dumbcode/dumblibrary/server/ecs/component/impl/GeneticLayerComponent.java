package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.*;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.JavaUtils;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class GeneticLayerComponent extends EntityComponent implements RenderLayerComponent, FinalizableComponent, GatherGeneticsComponent {

    private final List<GeneticLayerEntry> entries = new ArrayList<>();

    private static final TextureManager RENDER_ENGINE = Minecraft.getMinecraft().renderEngine;

    private RenderLocationComponent.ConfigurableLocation baseLocation = null;

    @Override
    public void gatherLayers(Consumer<Consumer<Runnable>> registry) {
        for (GeneticLayerEntry entry : this.entries) {
            registry.accept(runnable -> {
                if(this.baseLocation != null) {
                    RENDER_ENGINE.bindTexture(entry.getTextureLocation(this.baseLocation));
                    GlStateManager.color(entry.colours[0], entry.colours[1], entry.colours[2], 1F);
                    runnable.run();
                    GlStateManager.color(1F, 1F, 1F, 1F);
                }
            });
        }
    }

    public void setLayerValues(String layer, float value) {
        int actValue = (int) (MathUtils.bounce(0, 1F, value) * 512F);
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.setColours(new float[] {
                    ((actValue     ) & 7) / 7F, //7 -> 0b111
                    ((actValue >> 3) & 7) / 7F,
                    ((actValue >> 6) & 7) / 7F
                });
            }
        }
        this.syncToClient();
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.entries.clear();
        StreamUtils.stream(compound.getTagList("entries", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticLayerEntry.deserailize((NBTTagCompound) b))
            .forEach(this.entries::add);
        super.deserialize(compound);
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setTag("entries", this.entries.stream().map(g -> GeneticLayerEntry.serialize(g, new NBTTagCompound())).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.entries.size());
        for (GeneticLayerEntry entry : this.entries) {
            GeneticLayerEntry.serialize(entry, buf);
        }
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.entries.clear();
        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.entries.add(GeneticLayerEntry.deserailize(buf));
        }
        super.deserialize(buf);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        entity.get(EntityComponentTypes.MODEL).ifPresent(m -> this.baseLocation = m.getTexture());
    }

    @Override
    public void gatherGenetics(Consumer<GeneticEntry> registry) {
        this.entries.stream()
            .map(GeneticLayerEntry::getLayerName)
            .forEach(n -> registry.accept(new GeneticEntry<>(GeneticTypes.LAYER_COLORS, new GeneticTypeLayerColorStorage().setLayerName(n), 128, 128).setRandomModifier()));
    }

    public static class Storage implements EntityComponentStorage<GeneticLayerComponent> {

        private final List<GeneticLayerEntry> entries = new ArrayList<>();

        public Storage addLayer(String layerName, String... locationsSuffix) {
            this.entries.add(new GeneticLayerEntry(layerName, locationsSuffix));
            return this;
        }

        @Override
        public GeneticLayerComponent construct() {
            GeneticLayerComponent component = new GeneticLayerComponent();
            component.entries.addAll(this.entries);
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("entries", this.entries.stream().map(e -> GeneticLayerEntry.serialize(e, new JsonObject())).collect(IOCollectors.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.entries.clear();
            StreamUtils.stream(JsonUtils.getJsonArray(json, "entries"))
                .map(e -> GeneticLayerEntry.deserailize(e.getAsJsonObject()))
                .forEach(this.entries::add);
        }
    }

    //todo:move to own class
    @Data
    @Accessors(chain = true)
    private static class GeneticLayerEntry {
        private final String layerName;
        private final String[] locationSuffix;

        private ResourceLocation textureLocationCache;

        private float[] colours = { 1, 1, 1 }; //Only used clientside.

        private GeneticLayerEntry(String layerName, String... locationsSuffix) {
            this.layerName = layerName;
            this.locationSuffix = locationsSuffix;
        }


        public ResourceLocation getTextureLocation(RenderLocationComponent.ConfigurableLocation baseLocation) {
            if(this.textureLocationCache == null) {
                this.textureLocationCache = TextureUtils.generateMultipleTexture(Arrays.stream(this.locationSuffix).map(s -> baseLocation.copy().addFileName(s, Integer.MAX_VALUE).getLocation()).toArray(ResourceLocation[]::new));

            }
            return this.textureLocationCache;
        }

        public static void serialize(GeneticLayerEntry entry, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entry.layerName);
            buf.writeShort(entry.getLocationSuffix().length);
            for (String suffix : entry.locationSuffix) {
                ByteBufUtils.writeUTF8String(buf, suffix);
            }

            for (float colour : entry.colours) {
                buf.writeFloat(colour);
            }
        }

        public static NBTTagCompound serialize(GeneticLayerEntry entry, NBTTagCompound tag) {
            tag.setString("layer", entry.layerName);
            tag.setTag("locations", Arrays.stream(entry.locationSuffix).map(NBTTagString::new).collect(IOCollectors.toNBTTagList()));

            tag.setFloat("colour_r", entry.colours[0]);
            tag.setFloat("colour_g", entry.colours[1]);
            tag.setFloat("colour_b", entry.colours[2]);
            return tag;
        }

        public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
            json.addProperty("layer", entry.layerName);
            json.add("locations", Arrays.stream(entry.locationSuffix).map(JsonPrimitive::new).collect(IOCollectors.toJsonArray()));
            return json;
        }

        public static GeneticLayerEntry deserailize(NBTTagCompound nbt) {
            return new GeneticLayerEntry(
                nbt.getString("layer"),
                StreamUtils.stream(nbt.getTagList("locations", Constants.NBT.TAG_STRING)).map(b -> ((NBTTagString)b).getString()).toArray(String[]::new)
            ).setColours(new float[]{ nbt.getFloat("colour_r"), nbt.getFloat("colour_g"), nbt.getFloat("colour_b") });
        }

        public static GeneticLayerEntry deserailize(JsonObject json) {
            return new GeneticLayerEntry(
                JsonUtils.getString(json, "layer"),
                StreamUtils.stream(JsonUtils.getJsonArray(json, "locations")).map(JsonElement::getAsString).toArray(String[]::new)
            );
        }

        public static GeneticLayerEntry deserailize(ByteBuf buf) {
            return new GeneticLayerEntry(
                ByteBufUtils.readUTF8String(buf),
                IntStream.range(0, buf.readShort()).mapToObj(i -> ByteBufUtils.readUTF8String(buf)).toArray(String[]::new)
            ).setColours(new float[]{ buf.readFloat(), buf.readFloat(), buf.readFloat() });
        }

    }
}
