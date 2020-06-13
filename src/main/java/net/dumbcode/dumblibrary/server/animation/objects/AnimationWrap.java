package net.dumbcode.dumblibrary.server.animation.objects;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.server.TickHandler;
import net.dumbcode.dumblibrary.server.animation.interpolation.Interpolation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

@Getter
public class AnimationWrap {
    private final AnimationEntry entry;
    private final AnimationLayer layer;

    private final Map<String, CubeWrapper> cubeMap = new HashMap<>();

    private final Deque<PoseData> poseStack = new ArrayDeque<>();

    private final float totalPoseTime;

    private float animationTicks;
    private float animationPartialTicks;

    private float maxTicks;
    private float tick;
    private float ci;

    private boolean invalidated;

    public AnimationWrap(AnimationEntry entry, AnimationLayer layer) {
        this.entry = entry;
        this.layer = layer;

        this.animationTicks = TickHandler.getTicks();

        this.poseStack.addAll(this.layer.getAnimationDataGetter().apply(entry.getAnimation()));
        if(this.poseStack.isEmpty()) {
            this.invalidated = true;
        } else {
            this.maxTicks = this.getData().getTime();
        }

        float tpt = 0;
        for (PoseData poseData : this.poseStack) {
            tpt += poseData.getTime();
        }
        this.totalPoseTime = tpt;

        if(!this.invalidated) {
            this.incrementVecs(false);
        }
    }

    public void tick(float partialTicks) {
        if (this.invalidated) { // && !this.entry.hold
            return;
        }

        float perc = this.tick / this.maxTicks;

//        this.ci = MathHelper.clamp(this.entry.isUseInertia() && perc <= 1F ? (float) (Math.sin(Math.PI * (perc - 0.5D)) * 0.5D + 0.5D) : perc, 0, 1);

        this.ci = MathHelper.clamp(perc, 0, 1);

        float factor = this.entry.getDegreeFactor().tryApply(this.layer.getObject(), AnimationFactor.Type.ANGLE, partialTicks);

        for (String partName : this.layer.getCubeNames()) {
            CubeWrapper cubeWrapper = Objects.requireNonNull(this.getCube(partName));
            AnimatableCube cube = this.layer.getAnimatableCube(partName);

            float[] interpolatedRotation = this.entry.getInterpolation().getInterpRot(cubeWrapper, ci);
            float[] interpolatedPosition = this.entry.getInterpolation().getInterpPos(cubeWrapper, ci);

            float[] rotation = cube.getDefaultRotation();
            cube.addRotation(
                (interpolatedRotation[0] - rotation[0]) * factor,
                (interpolatedRotation[1] - rotation[1]) * factor,
                (interpolatedRotation[2] - rotation[2]) * factor
            );


            float[] positions = cube.getDefaultRotationPoint();
            cube.addRotationPoint(
                (interpolatedPosition[0] - positions[0]) * factor,
                (interpolatedPosition[1] - positions[1]) * factor,
                (interpolatedPosition[2] - positions[2]) * factor
            );
        }

        float timeModifier = 1f;
        if(this.entry.getTime() > 0) {
            timeModifier = this.entry.getTime() / this.totalPoseTime;
        }

        timeModifier /= this.entry.getSpeedFactor().tryApply(this.layer.getObject(), AnimationFactor.Type.SPEED, partialTicks);
        timeModifier /= this.entry.getSpeed();

        float ticks = TickHandler.getTicks();
        this.tick += ((ticks-this.animationTicks) + (partialTicks-this.animationPartialTicks)) / timeModifier;//todo: Check that looping and holding work

        //Make sure to catchup to correct render
        while (!this.invalidated && this.tick >= this.maxTicks && (!this.entry.isHold() || this.poseStack.size() > 1)) {
            this.poseStack.pop();
            if (this.poseStack.isEmpty()) {
                if (this.entry.getTime() == AnimationLayer.LOOP) {
                    this.poseStack.addAll(this.layer.getPoseData(this.entry.getAnimation()));
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
        this.animationTicks = ticks;
        this.animationPartialTicks = partialTicks;
    }

    private void incrementVecs(boolean updatePrevious) {
        for (Map.Entry<String, CubeReference> mapEntry : this.getData().getCubes().entrySet()) {
            CubeWrapper cube = this.getCube(mapEntry.getKey());
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
        for (String name : this.layer.getCubeNames()) {
            CubeWrapper cube = this.getCube(name);

            float[] rot = this.entry.getInterpolation().getInterpRot(cube, ci);
            float[] pos = this.entry.getInterpolation().getInterpPos(cube, ci);

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

    public CubeWrapper getCube(String cube) {
        return this.cubeMap.computeIfAbsent(cube, s -> new CubeWrapper(this.layer.getAnicubeRef().apply(s)));
    }

    private PoseData getData() {
        return Objects.requireNonNull(this.poseStack.peek());
    }
}
