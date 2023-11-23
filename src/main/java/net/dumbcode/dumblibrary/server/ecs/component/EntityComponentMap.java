package net.dumbcode.dumblibrary.server.ecs.component;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public ListNBT serialize(ListNBT list) {
        for (Map.Entry<EntityComponentType<?, ?>, EntityComponent> entry : this.entrySet()) {
            CompoundTag componentTag = entry.getValue().serialize(new CompoundTag());
            componentTag.putString("identifier", entry.getKey().getIdentifier().toString());
            list.add(componentTag);
        }
        return list;
    }

    public void deserialize(ListNBT list) {
        this.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag componentTag = list.getCompound(i);
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

    public void serialize(FriendlyByteBuf buf) {
        buf.writeShort(this.size());
        for (Map.Entry<EntityComponentType<?, ?>, EntityComponent> entry : this.entrySet()) {
            buf.writeRegistryId(entry.getKey());
            entry.getValue().serialize(buf);
        }
    }

    public void deserialize(FriendlyByteBuf buf) {
        this.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            EntityComponentType<?, ?> type = buf.readRegistryIdSafe(EntityComponentType.getWildcardType());
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
