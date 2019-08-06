package net.dumbcode.dumblibrary.server.animation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.animation.data.PoseData;
import net.minecraft.entity.Entity;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer {

    public static final int LOOP = -2;
    public static final int RUN_TILL_COMPLETE = -1;

    private final Entity entity;
    //todo: now that we don't need to abstract this as much, maybe cut back on the amount of functions needed
    private final Function<Animation, List<PoseData>> animationDataGetter;
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;

    private final List<AnimationWrap> animations = Lists.newArrayList();
    private final Map<String, List<GhostAnimationData>> ghostWraps = Maps.newHashMap();

    public AnimationLayer(Entity entity, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, Function<Animation, List<PoseData>> animationDataGetter) {
        this.entity = entity;
        this.animationDataGetter = animationDataGetter;
        this.anicubeRef = anicubeRef;
        this.cubeNames = cubeNames;
    }

    public void animate(float ticks) {
        this.checkInvalidations();

        this.adjustForGhostWraps(ticks);

        for (AnimationWrap wrap : this.animations) {
            wrap.tick(ticks);
        }
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
                    CubeWrapper cube = wrap.getCuberef().apply(name);
                    this.ghostWraps.computeIfAbsent(name, s -> Lists.newArrayList()).add(
                            new GhostAnimationData(wrap.getInterpolation().getInterpPos(cube, wrap.getCi()), wrap.getInterpolation().getInterpRot(cube, wrap.getCi()), 1F, animation.getEntityAge()));
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

    public void removeAll() {
        this.animations.clear();
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
            AnimatableCube cube = this.anicubeRef.apply(cubeName);
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

    public boolean isPlaying(Animation animation) {
        for (AnimationWrap wrap : this.animations) {
            if (wrap.entry.getAnimation() == animation) {
                return true;
            }
        }
        return false;
    }

    public AnimationWrap create(AnimationEntry entry) {

        //Do we really need this as a cached map?
        //yes
        Map<String, CubeWrapper> cacheMap = new HashMap<>();
        return new AnimationWrap(entity, entry, this.animationDataGetter, s -> cacheMap.computeIfAbsent(s, o -> new CubeWrapper(this.anicubeRef.apply(o))), this.anicubeRef, this.cubeNames, this.entity.ticksExisted);
    }

    @Getter
    public static class CubeWrapper {
        private final Vector3f rotationPoint = new Vector3f();
        private final Vector3f prevRotationPoint = new Vector3f();
        private final Vector3f rotation = new Vector3f();
        private final Vector3f prevRotation = new Vector3f();

        CubeWrapper(AnimatableCube box) {
            float[] point = box.getDefaultRotationPoint();
            float[] defaultRotation = box.getDefaultRotation();
            this.rotationPoint.x = this.prevRotationPoint.x = point[0];
            this.rotationPoint.y = this.prevRotationPoint.y = point[1];
            this.rotationPoint.z = this.prevRotationPoint.z = point[2];
            this.rotation.x = this.prevRotation.x = defaultRotation[0];
            this.rotation.y = this.prevRotation.y = defaultRotation[1];
            this.rotation.z = this.prevRotation.z = defaultRotation[2];
        }
    }

    @AllArgsConstructor
    private class GhostAnimationData {
        float[] positions;
        float[] rotations;
        float ci;
        float minAge;
    }
}
