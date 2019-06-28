package net.dumbcode.dumblibrary.server.entity.component.impl;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AnimationComponent<E extends Entity & ComponentAccess> implements EntityComponent {

    @Getter private Function<Entity, AnimationContainer> animationContainer;

    private AnimationLayer animationLayer;

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
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation(0, (E) entity, newWrap.getEntry(), channel), e.world.provider.getDimension());
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound; //TODO: serialize the animation ?
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
    }

    @Override
    public void serialize(ByteBuf buf) {
    }

    @Override
    public void deserialize(ByteBuf buf) {
    }

    public AnimationLayer getAnimationLayer(Entity e) {
        if(this.animationLayer == null) {
            ComponentAccess entity = (ComponentAccess) e;
            Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(entity.get(EntityComponentTypes.MODEL).map(com -> com.getFileLocation().getLocation()).orElse(TabulaUtils.MISSING));
            this.animationLayer = new AnimationLayer(e, cubes.keySet(), cubes::get, animation -> new ArrayList<>(), true);//this.animationContainer.apply(entity).getAnimations()::get
        }
        return animationLayer;
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<AnimationComponent> {

        private Function<ComponentAccess, AnimationContainer> animationContainer;

        @Override
        public AnimationComponent construct() {
            AnimationComponent component = new AnimationComponent<>();
            component.animationContainer = Objects.requireNonNull(this.animationContainer, "Need an animation container to work");
            return component;
        }
    }
}
