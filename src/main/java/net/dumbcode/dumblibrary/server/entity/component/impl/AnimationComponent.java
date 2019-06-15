package net.dumbcode.dumblibrary.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationRunWrapper;
import net.dumbcode.dumblibrary.server.animation.objects.MultiAnimationLayer;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Map;
import java.util.Objects;

public class AnimationComponent<E extends Entity> implements EntityComponent {

    @Getter private AnimationRunWrapper<E> animationWrapper = null;
    private MultiAnimationLayer<E> animationLayer;

    public ModelGetter<E> modelGetter;


    private AnimationLayer.AnimationWrap[] layersActive = new AnimationLayer.AnimationWrap[Byte.MAX_VALUE];

    /**
     * Plays the animation on a certain channel
     * @param entity the entity
     * @param entry the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    @SuppressWarnings("unchecked")
    public void playAnimation(ComponentAccess entity, AnimationLayer.AnimationEntry entry, int channel) {
        this.playAnimation(entity, this.animationLayer.create(entry), channel);
    }

    /**
     * Plays the animation on a certain channel
     * @param entity the entity
     * @param newWrap the animation wrap
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    @SuppressWarnings("unchecked")
    public void playAnimation(ComponentAccess entity, AnimationLayer.AnimationWrap newWrap, int channel) {
        if(channel >= 0) {
            AnimationLayer.AnimationWrap current = this.layersActive[channel];
            if(current != null) {
                this.animationLayer.removeAnimation(current);
            }
        }
        this.animationLayer.addAnimation(newWrap);
        if(channel >= 0) {
            this.layersActive[channel] = newWrap;
        }
        Entity e = (Entity) entity;
        if(!e.world.isRemote) {
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation(0, (E) entity, this.modelGetter.getInfo((E) entity), newWrap.getEntry(), channel), e.world.provider.getDimension());
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("model_getter_id", this.modelGetter.registryName.toString());
        compound.setTag("model_getter", this.modelGetter.serialize(new NBTTagCompound()));
        return compound; //TODO: serialize the animation ?
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(NBTTagCompound compound) {
        this.modelGetter = modelGetterRegistry.get(new ResourceLocation(compound.getString("model_getter_id")));
        this.modelGetter.deserialize(compound.getCompoundTag("model_getter"));
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.modelGetter.registryName.toString());
        this.modelGetter.serialize(buf);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deserialize(ByteBuf buf) {
        this.modelGetter = modelGetterRegistry.get(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
        this.modelGetter.deserialize(buf);
    }

    public void initAnimationData(AnimationRunWrapper<E> wrapper, MultiAnimationLayer<E> layer) {
        this.animationWrapper = wrapper;
        this.animationLayer = layer;
    }

    public void createServersideWrapper(E entity) {
        if(this.modelGetter != null) {
            Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(this.modelGetter.getLocation(entity));
            MultiAnimationLayer<E> layer = new MultiAnimationLayer<>(entity, cubes.keySet(), cubes::get, this.modelGetter.getInfo(entity), true);
            this.initAnimationData(new AnimationRunWrapper<>(entity, Lists.newArrayList(layer)), layer);
        }
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<AnimationComponent> {

        private ModelGetter modelGetter;

        @Override
        public AnimationComponent construct() {
            AnimationComponent component = new AnimationComponent<>();
            component.modelGetter = Objects.requireNonNull(this.modelGetter, "No way to get model has been set at construction");
            return component;
        }
    }

    public static Map<ResourceLocation, ModelGetter> modelGetterRegistry = Maps.newHashMap();

    public abstract static class ModelGetter<E extends Entity> {

        final ResourceLocation registryName;

        public ModelGetter(ResourceLocation registryName) {
            this.registryName = registryName;
            modelGetterRegistry.put(this.registryName, this);
        }

        public abstract ResourceLocation getLocation(E entity);

        public abstract AnimationSystemInfo<E> getInfo(E entity);

        public NBTTagCompound serialize(NBTTagCompound compound) {
            return compound;
        }

        public void deserialize(NBTTagCompound compound) {
        }

        public void serialize(ByteBuf buf) {
        }

        public void deserialize(ByteBuf buf) {
        }
    }
}
