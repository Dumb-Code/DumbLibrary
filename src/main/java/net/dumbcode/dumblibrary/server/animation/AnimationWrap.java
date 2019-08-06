package net.dumbcode.dumblibrary.server.animation;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.animation.data.AnimationFactor;
import net.dumbcode.dumblibrary.server.animation.data.CubeReference;
import net.dumbcode.dumblibrary.server.animation.data.PoseData;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.dumbcode.dumblibrary.server.animation.interpolation.LinearInterpolation;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

import static net.dumbcode.dumblibrary.server.animation.AnimationLayer.LOOP;

@Getter
public class AnimationWrap {

    protected AnimationEntry entry;

    private final Entity entity;

    private final Function<Animation, List<PoseData>> animationDataGetter;
    private final Function<String, AnimationLayer.CubeWrapper> cuberef;
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;

    private Interpolation interpolation;

    private final Deque<PoseData> poseStack = new ArrayDeque<>();

    private final float totalPoseTime;

    private float entityAge;

    private float maxTicks;
    private float tick;
    private float ci;

    private boolean invalidated;

    public AnimationWrap(Entity entity, AnimationEntry animation, Function<Animation, List<PoseData>> animationDataGetter, Function<String, AnimationLayer.CubeWrapper> cuberef, Function<String, AnimatableCube> anicubeRef, Collection<String> cubeNames , float age) {
        this.entity = entity;
        this.animationDataGetter = animationDataGetter;
        this.cuberef = cuberef;
        this.anicubeRef = anicubeRef;
        this.cubeNames = cubeNames;
        this.entityAge = age;
        this.entry = animation;
        this.poseStack.addAll(this.animationDataGetter.apply(animation.getAnimation()));
        this.maxTicks = this.getData().getTime();
        this.interpolation = new LinearInterpolation();

        float tpt = 0;
        for (PoseData poseData : this.poseStack) {
            tpt += poseData.getTime();
        }
        this.totalPoseTime = tpt;

        this.incrementVecs(false);
    }

    public void tick(float age) {
        if (this.invalidated) { // && !this.entry.hold
            return;
        }

        float perc = this.tick / this.maxTicks;

        this.ci = MathHelper.clamp(this.entry.isUseInertia() && perc <= 1F ? (float) (Math.sin(Math.PI * (perc - 0.5D)) * 0.5D + 0.5D) : perc, 0, 1);

        for (String partName : this.cubeNames) {
            AnimationLayer.CubeWrapper cubeWrapper = Objects.requireNonNull(this.cuberef.apply(partName));
            AnimatableCube cube = this.anicubeRef.apply(partName);

            float[] interpolatedRotation = this.interpolation.getInterpRot(cubeWrapper, ci);
            float[] interpolatedPosition = this.interpolation.getInterpPos(cubeWrapper, ci);

            float[] rotation = cube.getDefaultRotation();
            float factor = this.entry.getDegreeFactor().getDegree((ComponentAccess) this.entity, AnimationFactor.Type.ANGLE, age % 1F);
            cube.addRotation(
                    (interpolatedRotation[0] - rotation[0]) * factor,
                    (interpolatedRotation[1] - rotation[1]) * factor,
                    (interpolatedRotation[2] - rotation[2]) * factor
            );


            float[] positions = cube.getDefaultRotationPoint();
            cube.addRotationPoint(
                    interpolatedPosition[0] - positions[0],
                    interpolatedPosition[1] - positions[1],
                    interpolatedPosition[2] - positions[2]
            );
        }

        float timeModifier = 1f;
        if(this.entry.getTime() > 0) {
            timeModifier = this.entry.getTime() / this.totalPoseTime;
        }

        timeModifier /= this.entry.getSpeedFactor().getDegree((ComponentAccess) this.entity, AnimationFactor.Type.SPEED, age % 1F);

        this.tick += (age - this.entityAge) / timeModifier;//todo: Check that looping and holding work

        //Make sure to catchup to correct render
        while (!this.invalidated && this.tick >= this.maxTicks && (!this.entry.isHold() || this.poseStack.size() > 1)) {
            this.poseStack.pop();
            if (this.poseStack.isEmpty()) {
                if (this.entry.getTime() == LOOP) {
                    this.poseStack.addAll(this.animationDataGetter.apply(this.entry.getAnimation()));
                } else {
                    this.invalidated = true;
                }
            }
            if (!this.invalidated) {
                this.tick -= this.maxTicks;
                this.maxTicks = this.getData().getTime();
                this.incrementVecs(true);
            }
        }
        this.entityAge = age;
    }

    private void incrementVecs(boolean updatePrevious) {
        for (Map.Entry<String, CubeReference> mapEntry : this.getData().getCubes().entrySet()) {
            AnimationLayer.CubeWrapper cube = Objects.requireNonNull(this.cuberef.apply(mapEntry.getKey()));
            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            Vector3f np = cube.getRotationPoint();
            Vector3f pp = cube.getPrevRotationPoint();

            if (updatePrevious) {
                pr.x = cr.x;
                pr.y = cr.y;
                pr.z = cr.z;

                pp.x = np.x;
                pp.y = np.y;
                pp.z = np.z;
            }

            CubeReference next = mapEntry.getValue();
            cr.x = next.getRotationX();
            cr.y = next.getRotationY();
            cr.z = next.getRotationZ();
            np.x = next.getRotationPointX();
            np.y = next.getRotationPointY();
            np.z = next.getRotationPointZ();
        }
    }

    public void onFinish() {
        this.invalidated = true;
        for (String name : this.cubeNames) {
            AnimationLayer.CubeWrapper cube = this.cuberef.apply(name);

            float[] rot = this.interpolation.getInterpRot(cube, ci);
            float[] pos = this.interpolation.getInterpPos(cube, ci);

            Vector3f pr = cube.getPrevRotation();
            Vector3f pp = cube.getPrevRotationPoint();

            pr.x = rot[0];
            pr.y = rot[1];
            pr.z = rot[2];

            pp.x = pos[0];
            pp.y = pos[1];
            pp.z = pos[2];

        }
    }

    private PoseData getData() {
        return Objects.requireNonNull(this.poseStack.peek());
    }
}