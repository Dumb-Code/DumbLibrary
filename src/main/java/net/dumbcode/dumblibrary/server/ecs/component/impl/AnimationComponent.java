package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.animation.AnimatedReferenceCube;
import net.dumbcode.dumblibrary.server.animation.Animation;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.network.S2CSyncAnimation;
import net.dumbcode.dumblibrary.server.network.S2CStopAnimation;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.dumbcode.studio.animation.info.AnimationEntryData;
import net.dumbcode.studio.animation.info.AnimationInfo;
import net.dumbcode.studio.animation.instance.AnimatedCube;
import net.dumbcode.studio.animation.instance.ModelAnimationHandler;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public class AnimationComponent extends EntityComponent implements RenderCallbackComponent, FinalizableComponent {

    @Setter @Getter AnimationContainer animationContainer;

    private final ModelAnimationHandler animationHandler = new ModelAnimationHandler();

    private final AnimationLayer[] layers = new AnimationLayer[Byte.MAX_VALUE];

    private BiConsumer<AnimationLayer, Integer> startSyncer;
    private IntConsumer stopSyncer;

    private Iterable<? extends AnimatedCube> modelCubes;

    /**
     * Returns whether a channel is active.
     * @param channel the channel to check
     * @return true if an animation is being played, false otherwise.
     */
    public boolean isChannelActive(int channel) {
        return this.layers[channel] != null && this.animationHandler.isPlaying(this.layers[channel].getUuid());
    }

    @Nullable
    public AnimationInfo getWrap(int channel) {
        return this.animationHandler.getInfo(this.layers[channel].getUuid());
    }

    public Animation getAnimation(int channel) {
        return this.layers[channel].getAnimation();
    }

    public AnimationInfo getInfo(Animation animation) {
        return this.animationContainer.getInfo(animation);
    }

    public AnimationEntryData playAnimation(Animation animation, int channel) {
        return this.playAnimation(animation, this.animationContainer.getInfo(animation).data(), channel);
    }
    /**
     * Plays the animation on a certain channel
     * @param animation the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    public AnimationEntryData playAnimation(Animation animation, AnimationEntryData entry, int channel) {
        if(channel >= 0) {
            UUID current = this.layers[channel].getUuid();
            if(current != null) {
                this.animationHandler.markRemoved(current);
            }
        }
        AnimationLayer layer = new AnimationLayer(animation, this.animationHandler.startAnimation(entry));
        if(channel >= 0) {
            this.layers[channel] = layer;
        }
        this.startSyncer.accept(layer, channel);

        return entry;
    }

    public void stopAnimation(int channel) {
        UUID id = this.layers[channel].getUuid();
        if(id != null) {
            this.animationHandler.markRemoved(id);
            this.layers[channel] = null;
            this.stopSyncer.accept(channel);
        }
    }

    public void stopAll() {
        Arrays.fill(this.layers, null);
        this.animationHandler.markAllRemoved();
    }

    @Deprecated
    public boolean isReadyForAnimations() {
        return true;
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof Entity) {
            Entity e = (Entity) entity;
            //if(e.world.isRemote) {
            //                ModelComponent component = entity.get(EntityComponentTypes.MODEL).orElseThrow(() -> new IllegalStateException("Animation component needs a model component"));
            //                this.animationLayer = SidedExecutor.getClient(() -> () -> {
            //                    DCMModel model = component.getModelCache();
            //                    return new AnimationLayer(model.getAllCubesNames(), model::getCube, this.animationContainer.createDataGetter(), e);
            //                }, null);
            //
            //            } else {
            //                Map<String, AnimatableCube> cubes = DCMUtils.getServersideCubes(entity.get(EntityComponentTypes.MODEL).map(com -> com.getFileLocation().getLocation()).orElse(DCMUtils.MISSING));
            //                this.animationLayer = new AnimationLayer(cubes.keySet(), cubes::get, this.animationContainer.createDataGetter(), e);//this.animationContainer.apply(ecs).getAnimations()::get
            //            }
            if (e.level.isClientSide) {
                ModelComponent component = entity.get(EntityComponentTypes.MODEL).orElseThrow(() -> new IllegalStateException("Animation component needs a model component"));
                SidedExecutor.getClient(() -> () -> component.getModelCache().getAllCubes(), Collections.emptyList());
            } else {
                this.startSyncer = (data, channel) -> DumbLibrary.NETWORK.send(PacketDistributor.DIMENSION.with(e.level::dimension), new S2CSyncAnimation(e.getId(), data.getAnimation(), channel));
                this.stopSyncer = channel -> DumbLibrary.NETWORK.send(PacketDistributor.DIMENSION.with(e.level::dimension), new S2CStopAnimation(e.getId(), channel));
                Map<String, AnimatedReferenceCube> cubes = DCMUtils.getServersideCubes(entity.get(EntityComponentTypes.MODEL).map(com -> com.getFileLocation().getLocation()).orElse(DCMUtils.MISSING));
            }
        } else {
            throw new IllegalStateException("Unable to animate non entity. Type " + entity.getClass().getSimpleName());
        }
    }

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        preRenderCallbacks.add((context, entity, x, y, z, entityYaw, partialTicks) -> this.animationHandler.animate(this.modelCubes, partialTicks));
    }

    @Value
    private static class AnimationLayer {
        private final Animation animation;
        private final UUID uuid;
    }
}
