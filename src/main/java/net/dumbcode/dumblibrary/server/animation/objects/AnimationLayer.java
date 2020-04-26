package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.dumbcode.dumblibrary.server.animation.interpolation.LinearInterpolation;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
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
    private Function<String, AnimatableCube> anicubeRef;
    private Collection<String> cubeNames;

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

        for (AnimationWrap wrap : this.animations) {
            wrap.setFromModel(model);
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
        return new AnimationWrap(entry, this.animationDataGetter, s -> cacheMap.computeIfAbsent(s, o -> new CubeWrapper(this.anicubeRef.apply(o))), this.anicubeRef, this.cubeNames, this.entity);
    }


    @AllArgsConstructor
    private static class GhostAnimationData {
        float[] positions;
        float[] rotations;
        float ci;
        float minAge;
    }
}
