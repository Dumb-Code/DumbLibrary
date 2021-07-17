package net.dumbcode.dumblibrary.server.ecs.component.impl.data;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.IResource;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

//todo:move to own class
@Data
@AllArgsConstructor(access = AccessLevel.NONE)
@Accessors(chain = true)
public class GeneticLayerEntry {
    private final String layerName;
    private final float index;
    private final boolean checkIfExists;
    private final boolean variesOpacity;

    private final float defaultColorMin;
    private final float defaultColorMax;
    private final float defaultAlphaMin;
    private final float defaultAlphaMax;

    private ResourceLocation textureLocationCache;

    private float[] colorCache = null;
    private final Map<UUID, Vector4f> baseTints = new HashMap<>();
    private final Map<UUID, Vector4f> targetTints = new HashMap<>();
    private Vector3f averageColor = null;
    private boolean doesTextureExist;

    public GeneticLayerEntry(String layerName, float index, boolean checkIfExists, boolean variesOpacity, float defaultColorMin, float defaultColorMax, float defaultAlphaMin, float defaultAlphaMax) {
        this.layerName = layerName;
        this.index = index;
        this.checkIfExists = checkIfExists;
        this.variesOpacity = variesOpacity;

        this.defaultColorMin = defaultColorMin;
        this.defaultColorMax = defaultColorMax;
        this.defaultAlphaMin = defaultAlphaMin;
        this.defaultAlphaMax = defaultAlphaMin;
    }

    public Optional<ResourceLocation> getTextureLocation(RenderLocationComponent.ConfigurableLocation baseLocation) {
        if (this.textureLocationCache == null) {
            this.textureLocationCache = baseLocation.copy().addFileName(this.layerName, Integer.MAX_VALUE).getLocation();
            if (this.checkIfExists) {
                this.doesTextureExist = Minecraft.getInstance().getResourceManager().hasResource(this.textureLocationCache);
            }
            if (!this.checkIfExists || this.doesTextureExist) {
                this.tryGetAverageColor();
            }
        }
        if (this.checkIfExists && !this.doesTextureExist) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.textureLocationCache);
    }

    public static GeneticLayerEntryBuilder builder(String layerName, float index) {
        return new GeneticLayerEntryBuilder(layerName, index);
    }

    private void tryGetAverageColor() {
        try (IResource resource = Minecraft.getInstance().getResourceManager().getResource(this.textureLocationCache)) {
            NativeImage read = NativeImage.read(resource.getInputStream());
            int[] data = read.makePixelArray();
            List<Vector3d> acceptablePixels = new ArrayList<>();
            for (int textureDatum : data) {
                if (((textureDatum >> 24) & 0xFF) != 0) {
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
                .ifPresent(v -> this.setAverageColor((float) v.x / size, (float) v.y / size, (float) v.z / size));
        } catch (IOException e) {
            DumbLibrary.getLogger().warn("Unable to get resource");
        }
    }

    public float[] getColours() {
        if (this.colorCache != null) {
            return this.colorCache;
        }
        List<Vector4f> tints = new ArrayList<>(this.baseTints.values());
        if (this.averageColor != null) {
            for (Vector4f value : this.targetTints.values()) {
                tints.add(new Vector4f(
                    value.x() / this.averageColor.x(),
                    value.y() / this.averageColor.y(),
                    value.z() / this.averageColor.z(),
                    value.w()
                ));
            }
        }
        if (tints.isEmpty()) {
            return this.colorCache = new float[]{1, 1, 1};
        }
        Vector4f result = new Vector4f();
        for (Vector4f tint : tints) {
            result.setX(result.x() + tint.x());
            result.setY(result.y() + tint.y());
            result.setZ(result.z() + tint.z());
            result.setW(result.w() + tint.w());
        }

        return this.colorCache = new float[]{result.x() / tints.size(), result.y() / tints.size(), result.z() / tints.size(), this.variesOpacity ? result.w() / tints.size() : 1F};
    }

    public void setAverageColor(float r, float b, float g) {
        this.averageColor = new Vector3f(r, g, b);
        this.colorCache = null;
    }

    public void addDirectTint(UUID uuid, float... colours) {
        Vector4f vector4f = new Vector4f();
        vector4f.set(colours);
        this.baseTints.put(uuid, vector4f);
        this.colorCache = null;
    }

    public void addTargetTint(UUID uuid, float... colours) {
        Vector4f vector4f = new Vector4f();
        vector4f.set(colours);
        this.baseTints.put(uuid, vector4f);
        this.colorCache = null;
    }

    public static void serialize(GeneticLayerEntry entry, PacketBuffer buf) {
        buf.writeUtf(entry.layerName);
        buf.writeFloat(entry.index);
        buf.writeBoolean(entry.checkIfExists);
        buf.writeBoolean(entry.variesOpacity);

        buf.writeFloat(entry.defaultColorMin);
        buf.writeFloat(entry.defaultColorMax);
        buf.writeFloat(entry.defaultAlphaMin);
        buf.writeFloat(entry.defaultAlphaMax);

        writeMap(entry.baseTints, buf);
        writeMap(entry.targetTints, buf);
    }

    private static void writeMap(Map<UUID, Vector4f> map, ByteBuf buf) {
        buf.writeByte(map.size());
        map.forEach((uuid, col) -> {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());

            buf.writeFloat(col.x());
            buf.writeFloat(col.y());
            buf.writeFloat(col.z());
            buf.writeFloat(col.w());
        });
    }

    public static CompoundNBT serialize(GeneticLayerEntry entry, CompoundNBT tag) {
        tag.putString("layer", entry.layerName);
        tag.putFloat("index", entry.index);
        tag.putBoolean("check_existing", entry.checkIfExists);
        tag.putBoolean("varies_opacity", entry.variesOpacity);

        tag.putFloat("default_color_min", entry.defaultColorMin);
        tag.putFloat("default_color_max", entry.defaultColorMax);
        tag.putFloat("default_alpha_min", entry.defaultAlphaMin);
        tag.putFloat("default_alpha_max", entry.defaultAlphaMax);

        tag.put("BaseTints", writeMap(entry.baseTints));
        tag.put("TargetTints", writeMap(entry.targetTints));


        return tag;
    }

    private static ListNBT writeMap(Map<UUID, Vector4f> map) {
        ListNBT list = new ListNBT();
        map.forEach((uuid, col) -> {
            CompoundNBT nbt = new CompoundNBT();

            nbt.putUUID("UUID", uuid);
            nbt.putFloat("r", col.x());
            nbt.putFloat("g", col.y());
            nbt.putFloat("b", col.z());
            nbt.putFloat("a", col.w());

            list.add(nbt);
        });
        return list;
    }

    public static JsonObject serialize(GeneticLayerEntry entry, JsonObject json) {
        json.addProperty("layer", entry.layerName);
        json.addProperty("index", entry.index);
        json.addProperty("check_existing", entry.checkIfExists);
        json.addProperty("varies_opacity", entry.variesOpacity);

        json.addProperty("default_color_min", entry.defaultColorMin);
        json.addProperty("default_color_max", entry.defaultColorMax);
        json.addProperty("default_alpha_min", entry.defaultAlphaMin);
        json.addProperty("default_alpha_max", entry.defaultAlphaMax);

        return json;
    }

    public static GeneticLayerEntry deserailize(CompoundNBT nbt) {
        GeneticLayerEntry entry = new GeneticLayerEntry(
            nbt.getString("layer"),
            nbt.getFloat("index"),
            nbt.getBoolean("check_existing"),
            nbt.getBoolean("varies_opacity"),

            nbt.getFloat("default_color_min"),
            nbt.getFloat("default_color_max"),
            nbt.getFloat("default_alpha_min"),
            nbt.getFloat("default_alpha_max")
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
                new float[]{tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b"), tag.getFloat("a")}
            );
        }
    }

    public static GeneticLayerEntry deserailize(JsonObject json) {
        return new GeneticLayerEntry(
            JSONUtils.getAsString(json, "layer"),
            JSONUtils.getAsFloat(json, "index"),
            JSONUtils.getAsBoolean(json, "check_existing"),
            JSONUtils.getAsBoolean(json, "varies_opacity"),
            JSONUtils.getAsFloat(json, "default_color_min"),
            JSONUtils.getAsFloat(json, "default_color_max"),
            JSONUtils.getAsFloat(json, "default_alpha_min"),
            JSONUtils.getAsFloat(json, "default_alpha_max")
        );
    }

    public static GeneticLayerEntry deserailize(PacketBuffer buf) {
        GeneticLayerEntry entry = new GeneticLayerEntry(
            buf.readUtf(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
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
                new float[]{buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()}
            );
        }
    }

    public static class GeneticLayerEntryBuilder {
        private final String layerName;
        private final float index;
        private boolean checkIfExists = false;
        private boolean variesOpacity = false;
        private float defaultColorMin = 0.7F;
        private float defaultColorMax = 1F;
        private float defaultAlphaMin = 0.5F;
        private float defaultAlphaMax = 1F;

        GeneticLayerEntryBuilder(String layerName, float index) {
            this.layerName = layerName;
            this.index = index;
        }

        public GeneticLayerEntryBuilder checkIfExists() {
            return this.checkIfExists(true);
        }

        public GeneticLayerEntryBuilder checkIfExists(boolean checkIfExists) {
            this.checkIfExists = checkIfExists;
            return this;
        }

        public GeneticLayerEntryBuilder variesOpacity() {
            return this.variesOpacity(true);
        }

        public GeneticLayerEntryBuilder variesOpacity(boolean variesOpacity) {
            this.variesOpacity = variesOpacity;
            return this;
        }

        public GeneticLayerEntryBuilder defaultColorMin(float defaultColorMin) {
            this.defaultColorMin = defaultColorMin;
            return this;
        }

        public GeneticLayerEntryBuilder defaultColorMax(float defaultColorMax) {
            this.defaultColorMax = defaultColorMax;
            return this;
        }

        public GeneticLayerEntryBuilder defaultAlphaMin(float defaultAlphaMin) {
            this.defaultAlphaMin = defaultAlphaMin;
            return this;
        }

        public GeneticLayerEntryBuilder defaultAlphaMax(float defaultAlphaMax) {
            this.defaultAlphaMax = defaultAlphaMax;
            return this;
        }

        public GeneticLayerEntry build() {
            return new GeneticLayerEntry(layerName, index, checkIfExists, variesOpacity, defaultColorMin, defaultColorMax, defaultAlphaMin, defaultAlphaMax);
        }

        @Override
        public String toString() {
            return "GeneticLayerEntry.GeneticLayerEntryBuilder(" +
                "layerName=" + this.layerName +
                ", index=" + this.index +
                ", checkIfExists=" + this.checkIfExists +
                ", variesOpacity=" + this.variesOpacity +
                ", defaultColorMin=" + this.defaultColorMin +
                ", defaultColorMax=" + this.defaultColorMax +
                ", defaultAlphaMin=" + this.defaultAlphaMin +
                ", defaultAlphaMax=" + this.defaultAlphaMax +
                ")";
        }
    }
}
