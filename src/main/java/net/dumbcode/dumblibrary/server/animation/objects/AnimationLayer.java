package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.registry.DumbRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;

    @Getter private final List<AnimationWrap> animations = Lists.newArrayList();
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
        return new AnimationWrap(entry, this.animationDataGetter, s -> cacheMap.computeIfAbsent(s, o -> new CubeWrapper(this.anicubeRef.apply(o))), this.anicubeRef, this.cubeNames, this.entity.ticksExisted);
    }


    @Getter
    public class AnimationWrap {
        protected AnimationEntry entry;

        private final Function<Animation, List<PoseData>> animationDataGetter;
        private final Function<String, CubeWrapper> cuberef;
        private final Function<String, AnimatableCube> anicubeRef;
        private final Collection<String> cubeNames;

        private final Deque<PoseData> poseStack = new ArrayDeque<>();

        private final float totalPoseTime;

        private float entityAge;

        private float maxTicks;
        private float tick;
        private float ci;

        private boolean invalidated;

        private AnimationWrap(AnimationEntry animation, Function<Animation, List<PoseData>> animationDataGetter, Function<String, CubeWrapper> cuberef, Function<String, AnimatableCube> anicubeRef, Collection<String> cubeNames , float age) {
            this.animationDataGetter = animationDataGetter;
            this.cuberef = cuberef;
            this.anicubeRef = anicubeRef;
            this.cubeNames = cubeNames;
            this.entityAge = age;
            this.entry = animation;
            this.poseStack.addAll(this.animationDataGetter.apply(animation.getAnimation()));
            this.maxTicks = this.getData().getTime();

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

            this.ci = MathHelper.clamp(this.entry.useInertia && perc <= 1F ? (float) (Math.sin(Math.PI * (perc - 0.5D)) * 0.5D + 0.5D) : perc, 0, 1);

            for (String partName : this.cubeNames) {
                CubeWrapper cubeWrapper = Objects.requireNonNull(this.cuberef.apply(partName));
                AnimatableCube cube = this.anicubeRef.apply(partName);

                float[] interpolatedRotation = this.getInterpRot(cubeWrapper);
                float[] interpolatedPosition = this.getInterpPos(cubeWrapper);

                float[] rotation = cube.getDefaultRotation();
                float factor = this.entry.degreeFactor.getDegree((ComponentAccess) AnimationLayer.this.entity, AnimationFactor.Type.ANGLE, age % 1F);
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
            if(this.entry.time > 0) {
                timeModifier = this.entry.time / this.totalPoseTime;
            }

            timeModifier /= this.entry.speedFactor.getDegree((ComponentAccess) AnimationLayer.this.entity, AnimationFactor.Type.SPEED, age % 1F);

            this.tick += (age - this.entityAge) / timeModifier;//todo: Check that looping and holding work

            //Make sure to catchup to correct render
            while (!this.invalidated && this.tick >= this.maxTicks && (!this.entry.hold || this.poseStack.size() > 1)) {
                this.poseStack.pop();
                if (this.poseStack.isEmpty()) {
                    if (this.entry.time == LOOP) {
                        this.poseStack.addAll(this.animationDataGetter.apply(this.entry.animation));
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
                CubeWrapper cube = Objects.requireNonNull(this.cuberef.apply(mapEntry.getKey()));
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
                CubeWrapper cube = this.cuberef.apply(name);

                float[] rot = this.getInterpRot(cube);
                float[] pos = this.getInterpPos(cube);

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

        public float[] getInterpRot(CubeWrapper cube) {
            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            return new float[] {
                    pr.x + (cr.x - pr.x) * this.ci,
                    pr.y + (cr.y - pr.y) * this.ci,
                    pr.z + (cr.z - pr.z) * this.ci
            };
        }

        public float[] getInterpPos(CubeWrapper cube) {
            Vector3f np = cube.getRotationPoint();
            Vector3f pp = cube.getPrevRotationPoint();

            return new float[] {
                    pp.x + (np.x - pp.x) * this.ci,
                    pp.y + (np.y - pp.y) * this.ci,
                    pp.z + (np.z - pp.z) * this.ci
            };
        }

        private PoseData getData() {
            return Objects.requireNonNull(this.poseStack.peek());
        }
    }

    @Getter
    @Wither
    public static class AnimationEntry {

        @NonNull
        private final Animation animation;
        private final int time; //-2 = loop, -1 = run until finished, x > 0 run for x amount of ticks
        private final boolean useInertia;
        private final boolean hold;
        private final AnimationFactor speedFactor;
        private final AnimationFactor degreeFactor;
        @Nullable
        private final AnimationEntry exitAnimation;

        public AnimationEntry(Animation animation) {
            this(animation, -1, animation.inertia(), animation.hold(), AnimationFactor.DEFAULT, AnimationFactor.DEFAULT, null);
        }

        public AnimationEntry(Animation animation, int time, boolean useInertia, boolean hold, AnimationFactor speedFactor, AnimationFactor degreeFactor, @Nullable AnimationEntry andThen) {
            this.animation = animation;
            this.time = time;
            this.useInertia = useInertia;
            this.hold = hold;
            this.speedFactor = speedFactor;
            this.degreeFactor = degreeFactor;
            this.exitAnimation = andThen;
        }

        public AnimationEntry loop() {
            return this.withTime(LOOP);
        }

        public void serialize(ByteBuf buf) {
            ByteBufUtils.writeRegistryEntry(buf, this.animation);
            buf.writeInt(this.time);
            buf.writeBoolean(this.useInertia);
            buf.writeBoolean(this.hold);
            ByteBufUtils.writeRegistryEntry(buf, this.speedFactor);
            ByteBufUtils.writeRegistryEntry(buf, this.degreeFactor);
            buf.writeBoolean(this.exitAnimation != null);
            if(this.exitAnimation != null) {
                this.exitAnimation.serialize(buf);
            }
        }

        public static AnimationEntry deserialize(ByteBuf buf) {
            return new AnimationEntry(
                    ByteBufUtils.readRegistryEntry(buf, DumbRegistries.ANIMATION_REGISTRY),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    ByteBufUtils.readRegistryEntry(buf, DumbRegistries.FLOAT_SUPPLIER_REGISTRY),
                    ByteBufUtils.readRegistryEntry(buf, DumbRegistries.FLOAT_SUPPLIER_REGISTRY),
                    buf.readBoolean() ? deserialize(buf) : null
                    );
        }
    }

    public interface AnimatableCube {
        float[] getDefaultRotationPoint();

        float[] getActualRotationPoint();

        float[] getDefaultRotation();

        float[] getActualRotation();

        float[] getOffset();

        float[] getDimension();

        void addRotationPoint(float pointX, float pointY, float pointZ);

        void addRotation(float rotationX, float rotationY, float rotationZ);

        void reset();

        @Nullable
        AnimatableCube getParent();
    }

    public static final class AnimatableCubeEmpty implements AnimatableCube {

        public static final AnimatableCubeEmpty INSTANCE = new AnimatableCubeEmpty();

        private AnimatableCubeEmpty() {

        }

        @Override
        public float[] getDefaultRotationPoint() {
            return new float[3];
        }

        @Override
        public float[] getActualRotationPoint() {
            return new float[3];
        }

        @Override
        public float[] getDefaultRotation() {
            return new float[3];
        }

        @Override
        public float[] getActualRotation() {
            return new float[3];
        }

        @Override
        public float[] getOffset() {
            return new float[3];
        }

        @Override
        public float[] getDimension() {
            return new float[3];
        }

        @Override
        public void addRotationPoint(float pointX, float pointY, float pointZ) {
            //NO OP
        }

        @Override
        public void addRotation(float rotationX, float rotationY, float rotationZ) {
            //NO OP
        }

        @Override
        public void reset() {
            //NO OP
        }

        @Nullable
        @Override
        public AnimatableCube getParent() {
            return null;
        }
    }


    @Getter
    static class CubeWrapper {
        private final Vector3f rotationPoint = new Vector3f();
        private final Vector3f prevRotationPoint = new Vector3f();
        private final Vector3f rotation = new Vector3f();
        private final Vector3f prevRotation = new Vector3f();


        CubeWrapper(AnimationLayer.AnimatableCube box) {
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
