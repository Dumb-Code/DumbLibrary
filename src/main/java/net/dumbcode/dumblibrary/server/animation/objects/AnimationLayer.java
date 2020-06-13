package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.TickHandler;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer {

    public static final int LOOP = -2;
    public static final int RUN_TILL_COMPLETE = -1;

    //todo: now that we don't need to abstract this as much, maybe cut back on the amount of functions needed
    private final Function<Animation, List<PoseData>> animationDataGetter;
    private Function<String, AnimatableCube> anicubeRef;
    private Collection<String> cubeNames;
    private final Object object;

    private final List<AnimationWrap> animations = Lists.newArrayList();
    private final Map<String, List<GhostAnimationData>> ghostWraps = Maps.newHashMap();

    public AnimationLayer(Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, Function<Animation, List<PoseData>> animationDataGetter, Object object) {
        this.animationDataGetter = animationDataGetter;
        this.anicubeRef = anicubeRef;
        this.cubeNames = cubeNames;
        this.object = object;
    }


    public void animate(float partialTicks) {
        this.checkInvalidations();

        this.adjustForGhostWraps(partialTicks);

        for (AnimationWrap wrap : this.animations) {
            wrap.tick(partialTicks);
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
            Interpolation interpolation = wrap.getEntry().getInterpolation();
            if (wrap == animation) {
                for (String name : this.cubeNames) {
                    CubeWrapper cube = wrap.getCube(name);
                    this.ghostWraps.computeIfAbsent(name, s -> Lists.newArrayList()).add(
                            new GhostAnimationData(interpolation.getInterpPos(cube, wrap.getCi()), interpolation.getInterpRot(cube, wrap.getCi()), 1F, animation.getAnimationTicks() + animation.getAnimationPartialTicks()));
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

    private void adjustForGhostWraps(float partialTicks) {
        float age = TickHandler.getTicks() + partialTicks;
        for (String cubeName : this.getCubeNames()) {
            AnimatableCube cube = this.anicubeRef.apply(cubeName);
            List<GhostAnimationData> ghosts = this.ghostWraps.getOrDefault(cubeName, Lists.newArrayList());
            for (GhostAnimationData data : ghosts) {
                data.ci = 1 - ((age-data.animationAge) / 7.5F); //Takes 7.5 ticks to go back.


                //TODO: make sure interpolation wraps around, ie an angle at 350 degrees shouldn't interpolate to 0, it should interpolate to 360
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

    @SideOnly(Side.CLIENT)
    public void setFromModel(TabulaModel model) {
        this.cubeNames.clear();
        this.cubeNames.addAll(model.getAllCubesNames());
        this.anicubeRef = model::getCube;
    }

    public boolean isPlaying(Animation animation) {
        for (AnimationWrap wrap : this.animations) {
            if (wrap.getEntry().getAnimation() == animation) {
                return true;
            }
        }
        return false;
    }

    public List<PoseData> getPoseData(Animation animation) {
        return this.animationDataGetter.apply(animation);
    }

    public AnimatableCube getAnimatableCube(String name) {
        return this.anicubeRef.apply(name);
    }

    public AnimationWrap create(AnimationEntry entry) {
        return new AnimationWrap(entry, this);
    }

    @AllArgsConstructor
    private static class GhostAnimationData {

        float[] positions;
        float[] rotations;
        float ci;
        float animationAge;
    }
}
