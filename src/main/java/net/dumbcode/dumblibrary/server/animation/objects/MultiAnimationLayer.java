package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultiAnimationLayer<E extends Entity> extends AnimationLayer<E> {

    @Getter private final List<AnimationWrap> animations = Lists.newArrayList();

    private final Map<String, List<GhostAnimationData>> ghostWraps = Maps.newHashMap();

    public MultiAnimationLayer(E entity, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, AnimationSystemInfo<E> info, boolean inertia) {
        super(entity, cubeNames, anicubeRef, info, inertia);
    }


    @Override
    public void animate(float ticks) {
        super.animate(ticks);

        this.checkInvalidations();

        this.adjustForGhostWraps(ticks);

        for (AnimationWrap wrap : this.animations) {
            wrap.tick(ticks);
        }
    }

    private void checkInvalidations() {
        for (int i = 0; i < this.animations.size(); i++) {
            AnimationWrap wrap = this.animations.get(i);
            if(wrap.isInvalidated()) {
                this.removeAnimation(wrap);
                i--;
            }
        }
    }

    private void adjustForGhostWraps(float ticks) {
        for (String cubeName : this.getCubeNames()) {
            AnimatableCube cube = this.getCurrentWrap().getAnicubeRef().apply(cubeName);
            List<GhostAnimationData> ghosts = this.ghostWraps.getOrDefault(cubeName, Lists.newArrayList());
            for (GhostAnimationData data : ghosts) {
                data.ci = 1 - ((ticks - data.minAge) / 7.5F); //Takes 7.5 ticks to go back.

                float[] rotation = cube.getDefaultRotation();
                cube.addRotation(
                        (data.rotations[0] - rotation[0]) * data.ci,
                        (data.rotations[1] - rotation[1]) * data.ci,
                        (data.rotations[2] - rotation[2]) * data.ci
                );


                float[] positions = cube.getDefaultRotationPoint();
                cube.addRotationPoint(
                        (data.positions[0] - positions[0]) * data.ci,
                        (data.positions[1] - positions[1]) * data.ci,
                        (data.positions[2] - positions[2]) * data.ci
                );

            }
            ghosts.removeIf(data -> data.ci <= 0);
        }
    }

    @Override
    public boolean canAnimate() {
        return false; //Don't want the normal animations to be done in any way.
    }

    @Override
    public Animation getAnimation() {
        return Animation.NONE;
    }

    @Override
    protected void onPoseIncremented() {
        this.ghostWraps.clear();
    }

    public void addAnimation(AnimationWrap wrap) {
        this.animations.add(wrap);
    }

    public void removeAnimation(AnimationWrap animation) {
        List<AnimationEntry> exitEntries = Lists.newArrayList();
        Iterator<AnimationWrap> iterator = this.animations.iterator();
        while (iterator.hasNext()) {
            AnimationWrap wrap = iterator.next();
            if (wrap == animation) {
                for (String name : wrap.getCubeNames()) {
                    AnimationRunWrapper.CubeWrapper cube = wrap.getCuberef().apply(name);
                    this.ghostWraps.computeIfAbsent(name, s -> Lists.newArrayList()).add(new GhostAnimationData(wrap.getInterpPos(cube), wrap.getInterpRot(cube), 1F, animation.getEntityAge()));
                }
                wrap.onFinish();
                if(wrap.getEntry().getExitAnimation() != null) {
                    exitEntries.add(wrap.getEntry().getExitAnimation());
                }
                iterator.remove();
            }
        }
        for (AnimationEntry entry : exitEntries) {
            this.addAnimation(this.create(entry));
        }

    }

    public boolean isPlaying(Animation animation) {
        for (AnimationWrap wrap : this.animations) {
            if (wrap.entry.getAnimation() == animation) {
                return true;
            }
        }
        return false;
    }

    @AllArgsConstructor
    private class GhostAnimationData {
        float[] positions;
        float[] rotations;
        float ci;
        float minAge;
    }



}