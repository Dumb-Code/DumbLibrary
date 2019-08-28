package net.dumbcode.dumblibrary.server.ecs.component.impl;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderCallbackComponent;
import net.dumbcode.dumblibrary.server.network.S0SyncAnimation;
import net.dumbcode.dumblibrary.server.utils.SidedExecutor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Map;

public class AnimationComponent<E extends Entity & ComponentAccess> extends EntityComponent implements RenderCallbackComponent {

    @Setter @Getter AnimationContainer animationContainer;

    @Getter private final List<FutureAnimation> futureAnimations = Lists.newArrayList();

    private AnimationLayer animationLayer;

    private AnimationLayer.AnimationWrap[] layersActive = new AnimationLayer.AnimationWrap[Byte.MAX_VALUE];

    /**
     * Plays the animation on a certain channel, after a series of time
     * @param entity the ecs
     * @param entry the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     * @param delay the delay, in ticks, until this animation should be played.
     */
    public void proposeAnimation(ComponentAccess entity, AnimationLayer.AnimationEntry entry, int channel, int delay) {
        this.futureAnimations.add(new FutureAnimation(entity, entry, channel, delay));
    }
    /**
     * Plays the animation on a certain channel
     * @param entity the ecs
     * @param entry the animation entry
     * @param channel if another animation at another channel is playing then that animation will be stopped. <br>
     *              If this is less than 0, then no animation will be stopped <br>
     *              The maximum channel is
     */
    public void playAnimation(ComponentAccess entity, AnimationLayer.AnimationEntry entry, int channel) {
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

    public void stopAll() {
        for (int i = 0; i < this.layersActive.length; i++) {
            this.layersActive[i] =  null;
        }
        this.animationLayer.removeAll();
    }

    /**
     * Is the animation component ready for playing animations
     * @return true if {@link #playAnimation(ComponentAccess, AnimationLayer.AnimationEntry, int)} can be called.
     * If it cannot be called, you should use {@link #proposeAnimation(ComponentAccess, AnimationLayer.AnimationEntry, int, int)}
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
                    ModelBase model = component.getModelCache();
                    if(model instanceof TabulaModel) {
                        TabulaModel tm = (TabulaModel) model;
                        return new AnimationLayer(e, tm.getAllCubesNames(), tm::getCube, this.animationContainer.getAnimations()::get);
                    } else {
                        //todo: make an implementation for non tabula models?
                        return new AnimationLayer(e, Lists.newArrayList(), s -> AnimationLayer.AnimatableCubeEmpty.INSTANCE, this.animationContainer.getAnimations()::get);

                    }
                }, null);

            } else {
                Map<String, AnimationLayer.AnimatableCube> cubes = TabulaUtils.getServersideCubes(entity.get(EntityComponentTypes.MODEL).map(com -> com.getFileLocation().getLocation()).orElse(TabulaUtils.MISSING));
                this.animationLayer = new AnimationLayer(e, cubes.keySet(), cubes::get, this.animationContainer.getAnimations()::get);//this.animationContainer.apply(ecs).getAnimations()::get
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

                this.animationLayer.animate(entity.ticksExisted + partialTicks);
            }

        });
    }

    @AllArgsConstructor
    public class FutureAnimation {
        private final ComponentAccess entity;
        private final AnimationLayer.AnimationEntry entry;
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
