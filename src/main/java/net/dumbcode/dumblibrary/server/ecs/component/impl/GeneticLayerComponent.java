package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeLayerColorStorage;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayer;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLayerComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.dumblibrary.server.utils.IndexedObject;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class GeneticLayerComponent extends EntityComponent implements RenderLayerComponent, FinalizableComponent, GatherGeneticsComponent {

    @Getter
    private final List<GeneticLayerEntry> entries = new ArrayList<>();

    private RenderLocationComponent.ConfigurableLocation baseLocation = null;

    private static final Random RAND = new Random();

    @Override
    public void gatherLayers(ComponentAccess entity, Consumer<IndexedObject<RenderLayer>> registry) {
        for (GeneticLayerEntry entry : this.entries) {
            registry.accept(new IndexedObject<>(new RenderLayer.DefaultTexture(() -> {
                if(this.baseLocation != null) {
                    float[] colours = entry.getColours();
                    return new RenderLayer.DefaultLayerData(entry.getTextureLocation(this.baseLocation), colours[0], colours[1], colours[2], 1F);
                }
                return null;
            }), entry.index));
        }
    }


    public void setLayerValues(String layer, float[] rgb) {
        for (GeneticLayerEntry entry : this.entries) {
            if(entry.getLayerName().equals(layer)) {
                entry.addDirectTint(new UUID(13, layer.hashCode()), rgb);
            }
        }
        this.syncToClient();
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.entries.clear();
        StreamUtils.stream(compound.getList("entries", Constants.NBT.TAG_COMPOUND))
            .map(b -> GeneticLayerEntry.deserailize((CompoundNBT) b))
            .forEach(this.entries::add);
        super.deserialize(compound);
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("entries", this.entries.stream().map(g -> GeneticLayerEntry.serialize(g, new CompoundNBT())).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeShort(this.entries.size());
        for (GeneticLayerEntry entry : this.entries) {
            GeneticLayerEntry.serialize(entry, buf);
        }
        super.serialize(buf);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
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
    public void gatherGenetics(ComponentAccess entity, Consumer<GeneticEntry> registry) {
        this.entries.stream()
            .map(GeneticLayerEntry::getLayerName)
            .forEach(n -> {
                GeneticEntry<?> entry = new GeneticEntry<>(GeneticTypes.LAYER_COLORS, "genetic_layer_" + n, new GeneticTypeLayerColorStorage().setLayerName(n), 128, 128);
                entry.setModifier(GeneticUtils.encode3BitColor(0.7F + RAND.nextFloat() * 0.3F, 0.7F + RAND.nextFloat() * 0.3F, 0.7F + RAND.nextFloat() * 0.3F));
                registry.accept(entry);
            });
    }

    public static class Storage implements EntityComponentStorage<GeneticLayerComponent> {

        private final List<GeneticLayerEntry> entries = new ArrayList<>();

        public Storage addLayer(String layerName, float index, String... locationsSuffix) {
            this.entries.add(new GeneticLayerEntry(layerName, index, locationsSuffix));
            return this;
        }

        @Override
        public void constructTo(GeneticLayerComponent component) {
            component.entries.addAll(this.entries);
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("entries", this.entries.stream().map(e -> GeneticLayerEntry.serialize(e, new JsonObject())).collect(CollectorUtils.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.entries.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "entries"))
                .map(e -> GeneticLayerEntry.deserailize(e.getAsJsonObject()))
                .forEach(this.entries::add);
        }
    }

    //todo:move to own class
    @Data
    @AllArgsConstructor(access = AccessLevel.NONE)
    @Accessors(chain = true)
    public static class GeneticLayerEntry {
        private final String layerName;
        private final float index;
        private final String[] locationSuffix;

        private ResourceLocation textureLocationCache;

        private float[] colorCache = null;
        private final Map<UUID, Vector3f> baseTints = new HashMap<>();
        private final Map<UUID, Vector3f> targetTints = new HashMap<>();
        private Vector3f averageColor = null;

        private GeneticLayerEntry(String layerName, float index, String... locationsSuffix) {
            this.layerName = layerName;
            this.index = index;
            this.locationSuffix = locationsSuffix;
        }

        public ResourceLocation getTextureLocation(RenderLocationComponent.ConfigurableLocation baseLocation) {
            if(this.textureLocationCache == null) {
                this.textureLocationCache = TextureUtils.generateMultipleTexture(Arrays.stream(this.locationSuffix).map(s -> baseLocation.copy().addFileName(s, Integer.MAX_VALUE).getLocation()).toArray(ResourceLocation[]::new));
                this.tryGetAverageColor();
            }
            return this.textureLocationCache;
        }

        private void tryGetAverageColor() {
            Texture texture = Minecraft.getInstance().textureManager.getTexture(this.textureLocationCache);
            if(texture instanceof DynamicTexture) {//Should be true
                int[] data = ((DynamicTexture) texture).getPixels().makePixelArray();
                List<Vector3d> acceptablePixels = new ArrayList<>();
                for (int textureDatum : data) {
                    if(((textureDatum >> 24) & 0xFF) != 0) {
                        acceptablePixels.add(new Vector3d(
                            ((textureDatum >> 16) & 0xFF) / 255F,
                            ((textureDatum >> 8) & 0xFF) / 255F,
                            (textureDatum & 0xFF) / 255F
                        ));
                    }
                }
                int size = acceptablePixels.size();
                acceptablePixels.stream()
                    .reduce(Vector3d::add)
                    .ifPresent(v -> this.setAverageColor((float)v.x/size, (float)v.y/size, (float)v.z/size));
            }
        }

        public float[] getColours() {
            if(this.colorCache != null) {
                return this.colorCache;
            }
            List<Vector3f> tints = new ArrayList<>(this.baseTints.values());
            if(this.averageColor != null) {
                for (Vector3f value : this.targetTints.values()) {
                    tints.add(new Vector3f(
                        value.x() / this.averageColor.x(),
                        value.y() / this.averageColor.y(),
                        value.z() / this.averageColor.z()
                    ));
                }
            }
            if(tints.isEmpty()) {
                return this.colorCache = new float[]{1, 1, 1};
            }
            Vector3f result = new Vector3f();
            tints.forEach(result::add);
            result.mul(1F / tints.size());

            return this.colorCache = new float[]{ result.x(), result.y(), result.z() };
        }

        public void setAverageColor(float r, float b, float g) {
            this.averageColor = new Vector3f(r, g, b);
            this.colorCache = null;
        }

        public void addDirectTint(UUID uuid, float... colours) {
            this.baseTints.put(uuid, new Vector3f(colours));
            this.colorCache = null;
        }

        public void addTargetTint(UUID uuid, float... colours) {
            this.baseTints.put(uuid, new Vector3f(colours));
            this.colorCache = null;
        }

        public static void serialize(GeneticLayerEntry entry, PacketBuffer buf) {
            buf.writeUtf(entry.layerName);
            buf.writeFloat(entry.index);
            buf.writeShort(entry.getLocationSuffix().length);
            for (String suffix : entry.locationSuffix) {
                buf.writeUtf(suffix);
            }
            writeMap(entry.baseTints, buf);
            writeMap(entry.targetTints, buf);
        }

        private static void writeMap(Map<UUID, Vector3f> map, ByteBuf buf) {
            buf.writeByte(map.size());
            map.forEach((uuid, col) -> {
                buf.writeLong(uuid.getMostSignificantBits());
                buf.writeLong(uuid.getLeastSignificantBits());

                buf.writeFloat(col.x());
                buf.writeFloat(col.y());
                buf.writeFloat(col.z());
            });
        }

        public static CompoundNBT serialize(GeneticLayerEntry entry, CompoundNBT tag) {
            tag.putString("layer", entry.layerName);
            tag.putFloat("index", entry.index);
            tag.put("locations", Arrays.stream(entry.locationSuffix).map(StringNBT::valueOf).collect(CollectorUtils.toNBTTagList()));

            tag.put("BaseTints", writeMap(entry.baseTints));
            tag.put("TargetTints", writeMap(entry.targetTints));

            return tag;
        }

        private static ListNBT writeMap(Map<UUID, Vector3f> map) {
            ListNBT list = new ListNBT();
            map.forEach((uuid, col) -> {
                CompoundNBT nbt = new CompoundNBT();

                nbt.putUUID("UUID", uuid);
                nbt.putFloat("r", col.x());
                nbt.putFloat("g", col.y());
                nbt.putFloat("b", col.z());

                list.add(nbt);
            });
            return list;
        }

        public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
            json.addProperty("layer", entry.layerName);
            json.addProperty("index", entry.index);
            json.add("locations", Arrays.stream(entry.locationSuffix).map(JsonPrimitive::new).collect(CollectorUtils.toJsonArray()));
            return json;
        }

        public static GeneticLayerEntry deserailize(CompoundNBT nbt) {
            GeneticLayerEntry entry = new GeneticLayerEntry(
                nbt.getString("layer"),
                nbt.getFloat("index"),
                StreamUtils.stream(nbt.getList("locations", Constants.NBT.TAG_STRING)).map(INBT::getAsString).toArray(String[]::new)
            );

            readMap(nbt.getList("BaseTints", Constants.NBT.TAG_STRING), entry::addDirectTint);
            readMap(nbt.getList("TargetTints", Constants.NBT.TAG_STRING), entry::addTargetTint);

            return entry;
        }

        private static void readMap(ListNBT list, BiConsumer<UUID, float[]> consumer) {
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                consumer.accept(
                    tag.getUUID("uuid"),
                    new float[]{ tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b") }
                );
            }
        }

        public static GeneticLayerEntry deserailize(JsonObject json) {
            return new GeneticLayerEntry(
                JSONUtils.getAsString(json, "layer"),
                JSONUtils.getAsFloat(json, "index"),
                StreamUtils.stream(JSONUtils.getAsJsonArray(json, "locations")).map(JsonElement::getAsString).toArray(String[]::new)
            );
        }

        public static GeneticLayerEntry deserailize(PacketBuffer buf) {
            GeneticLayerEntry entry = new GeneticLayerEntry(
                buf.readUtf(),
                buf.readFloat(),
                IntStream.range(0, buf.readShort()).mapToObj(i -> buf.readUtf()).toArray(String[]::new)
            );

            readMap(buf, entry::addDirectTint);
            readMap(buf, entry::addTargetTint);

            return entry;
        }

        private static void readMap(ByteBuf buf, BiConsumer<UUID, float[]> consumer) {
            byte size = buf.readByte();
            for (byte i = 0; i < size; i++) {
                consumer.accept(
                    new UUID(buf.readLong(), buf.readLong()),
                    new float[]{ buf.readFloat(), buf.readFloat(), buf.readFloat() }
                );
            }
        }
    }
}
