package net.dumbcode.dumblibrary.server.ecs.component;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class EntityComponentMap extends LinkedHashMap<EntityComponentType<?, ?>, EntityComponent> {

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getNullable(EntityComponentType<T, S> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return (T) component;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> Optional<T> get(EntityComponentType<T, S> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return Optional.of((T) component);
        }
        return Optional.empty();
    }

    public NBTTagList serialize(NBTTagList list) {
        for (Map.Entry<EntityComponentType<?, ?>, EntityComponent> entry : this.entrySet()) {
            NBTTagCompound componentTag = entry.getValue().serialize(new NBTTagCompound());
            componentTag.setString("identifier", entry.getKey().getIdentifier().toString());
            list.appendTag(componentTag);
        }
        return list;
    }

    public void deserialize(NBTTagList list) {
        this.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound componentTag = list.getCompoundTagAt(i);
            ResourceLocation identifier = new ResourceLocation(componentTag.getString("identifier"));
            EntityComponentType<?, ?> componentType = DumbRegistries.COMPONENT_REGISTRY.getValue(identifier);
            if (componentType != null) {
                EntityComponent component = componentType.constructEmpty();
                this.put(componentType, component);
                component.deserialize(componentTag);
            } else {
                DumbLibrary.getLogger().warn("Skipped invalid ecs component: '{}'", identifier);
            }
        }
    }

    public void serialize(ByteBuf buf) {
        buf.writeShort(this.size());
        for (Map.Entry<EntityComponentType<?, ?>, EntityComponent> entry : this.entrySet()) {
            ByteBufUtils.writeRegistryEntry(buf, entry.getKey());
            entry.getValue().serialize(buf);
        }
    }

    public void deserialize(ByteBuf buf) {
        this.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            EntityComponentType<?, ?> type = ByteBufUtils.readRegistryEntry(buf, DumbRegistries.COMPONENT_REGISTRY);
            EntityComponent component = type.constructEmpty();
            component.deserialize(buf);
            this.put(type, component);
        }
    }

    @Override
    public EntityComponent put(EntityComponentType<?, ?> key, EntityComponent value) {
        return super.put(key, value);
    }
}
