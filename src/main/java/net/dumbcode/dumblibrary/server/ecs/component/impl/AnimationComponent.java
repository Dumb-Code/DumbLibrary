package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.*;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.dumblibrary.server.network.S3StopAnimation;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//TODO: remove the E
public class AnimationComponent<E extends Entity & ComponentAccess> extends EntityComponent implements RenderCallbackComponent {

    @Setter @Getter AnimationContainer animationContainer;

    @Getter private final List<FutureAnimation> futureAnimations = Lists.newArrayList();

    private AnimationLayer animationLayer;

    private final AnimationWrap[] layersActive = new AnimationWrap[Byte.MAX_VALUE];

    /**
     * Plays the animation on a certain channel, after a series of time
     * @param entity the ecs
     * @param entry the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     * @param delay the delay, in ticks, until this animation should be played.
     */
    public void proposeAnimation(ComponentAccess entity, AnimationEntry entry, int channel, int delay) {
        this.futureAnimations.add(new FutureAnimation(entity, entry, channel, delay));
    }

    /**
     * Returns whether a channel is active.
     * @param channel the channel to check
     * @return true if an animation is being played, false otherwise.
     */
    public boolean isChannelActive(int channel) {
        return this.layersActive[channel] != null && !this.layersActive[channel].isInvalidated();
    }

    @Nullable
    public AnimationWrap getWrap(int channel) {
        return this.layersActive[channel];
    }

    /**
     * Plays the animation on a certain channel
     * @param entity the ecs
     * @param animation the animation
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    public void playAnimation(ComponentAccess entity, Animation animation, int channel) {
        this.playAnimation(entity, this.animationLayer.create(new AnimationEntry(animation)), channel);
    }

    /**
     * Plays the animation on a certain channel
     * @param entity the ecs
     * @param entry the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    public void playAnimation(ComponentAccess entity, AnimationEntry entry, int channel) {
        if(!this.isReadyForAnimations()) {
            this.proposeAnimation(entity, entry, channel, 10);
            return;
        }
        this.playAnimation(entity, this.animationLayer.create(entry), channel);
    }

    /**
     * Plays the animation on a certain channel
     * @param entity the ecs
     * @param newWrap the animation wrap
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    @SuppressWarnings("unchecked")
    public void playAnimation(ComponentAccess entity, AnimationWrap newWrap, int channel) {
        if(channel >= 0) {
            AnimationWrap current = this.layersActive[channel];
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
            DumbLibrary.NETWORK.sendToDimension(new S0SyncAnimation((E) entity, newWrap.getEntry(), channel), e.world.provider.getDimension());
        }
    }

    public void stopAnimation(Entity entity, int channel) {
        AnimationWrap wrap = this.layersActive[channel];
        if(wrap != null) {
            this.animationLayer.removeAnimation(wrap);
            this.layersActive[channel] = null;
            if(!entity.world.isRemote) {
                DumbLibrary.NETWORK.sendToDimension(new S3StopAnimation(entity, channel), entity.world.provider.getDimension());
            }
        }
    }

    public void stopAll() {
        Arrays.fill(this.layersActive, null);
        this.animationLayer.removeAll();
    }

    /**
     * Is the animation component ready for playing animations
     * @return true if {@link #playAnimation(ComponentAccess, AnimationEntry, int)} can be called.
     * If it cannot be called, you should use {@link #proposeAnimation(ComponentAccess, AnimationEntry, int, int)}
     */
    public boolean isReadyForAnimations() {
        return this.animationLayer != null;
    }

    public AnimationLayer getAnimationLayer(Entity e) {
        if(this.animationLayer == null) {
            ComponentAccess entity = (ComponentAccess) e;
            if(e.world.isRemote) {
                ModelComponent component = entity.get(EntityComponentTypes.MODEL).orElseThrow(() -> new IllegalStateException("Animation component needs a model component"));
                this.animationLayer = SidedExecutor.getClient(() -> () -> {
                    DCMModel model = component.getModelCache();
                    return new AnimationLayer(model.getAllCubesNames(), model::getCube, this.animationContainer.createDataGetter(), e);
                }, null);

            } else {
                Map<String, AnimatableCube> cubes = TabulaUtils.getServersideCubes(entity.get(EntityComponentTypes.MODEL).map(com -> com.getFileLocation().getLocation()).orElse(TabulaUtils.MISSING));
                this.animationLayer = new AnimationLayer(cubes.keySet(), cubes::get, this.animationContainer.createDataGetter(), e);//this.animationContainer.apply(ecs).getAnimations()::get
            }
        }
        return animationLayer;
    }

    @Override
    public void addCallbacks(List<SubCallback> preRenderCallbacks, List<MainCallback> renderCallbacks, List<SubCallback> postRenderCallback) {
        preRenderCallbacks.add((context, entity, x, y, z, entityYaw, partialTicks) -> {

            if(this.animationLayer != null) {
                for (String cubeName : this.animationLayer.getCubeNames()) {
                    this.animationLayer.getAnicubeRef().apply(cubeName).reset();
                }
                this.animationLayer.animate(partialTicks);
            }

        });
    }

    @AllArgsConstructor
    public class FutureAnimation {
        private final ComponentAccess entity;
        private final AnimationEntry entry;
        private final int channel;

        private int ticksLeft;

        /**
         * Ticks this future animations. If ticksLeft is 0 then the animation is played
         * @return true if the animation has been resolved and should be removed, false otherwise.
         */
        public boolean tick() {
            if(this.ticksLeft-- < 0) {
                AnimationComponent.this.playAnimation(this.entity, this.entry, this.channel);
                return true;
            }
            return false;
        }

    }
}
