package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Wither;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
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
public class AnimationLayer<E extends Entity> {

    public static final int LOOP = -2;
    public static final int RUN_TILL_COMPLETE = -1;

    private final E entity;
    private final AnimationSystemInfo<E> info;
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;
    private final boolean inertia;

    @Setter(AccessLevel.NONE)
    private AnimationWrap currentWrap;

    public AnimationLayer(E entity, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, AnimationSystemInfo<E> info, boolean inertia) {
        this.entity = entity;
        this.info = info;
        this.anicubeRef = anicubeRef;
        this.cubeNames = cubeNames;
        this.inertia = inertia;
        this.currentWrap = this.create(new AnimationEntry(Animation.NONE));
    }

    public void animate(float age) {
        if (this.canAnimate()) {
            Animation animation = this.getAnimation();
            if (animation != this.currentWrap.entry.animation) {
                this.setAnimation(animation, age);
            } else if (this.currentWrap.invalidated) {
                this.setAnimation(Animation.NONE, age);
            }
            this.currentWrap.tick(age);
        }
    }

    public Animation getAnimation() {
        return this.info.getAnimation(this.entity);
    }


    public void setAnimation(Animation animation, float age) {
        if (this.info.getPoseData(animation).isEmpty()) {
            animation = Animation.NONE;
        }
        this.currentWrap.onFinish();
        this.currentWrap = this.create(new AnimationEntry(animation, this.loop() ? LOOP : RUN_TILL_COMPLETE, animation.inertia(), animation.hold(), null));
    }

    public AnimationWrap create(AnimationEntry entry) {

        //Do we really need this as a cached map?
        //yes
        Map<String, AnimationRunWrapper.CubeWrapper> cacheMap = new HashMap<>();
        return new AnimationWrap(entry, this.info, s -> cacheMap.computeIfAbsent(s, o -> new AnimationRunWrapper.CubeWrapper(this.anicubeRef.apply(o))), this.anicubeRef, this.cubeNames, this.entity.ticksExisted);
    }

    public boolean canAnimate() {
        return true;
    }

    public boolean loop() {
        return false;
    }

    protected void onPoseIncremented() {
        //NO OP
    }

    @Getter
    public static class AnimationWrap {
        protected AnimationEntry entry;

        private final AnimationSystemInfo<?> info;
        private final Function<String, AnimationRunWrapper.CubeWrapper> cuberef;
        private final Function<String, AnimatableCube> anicubeRef;
        private final Collection<String> cubeNames;

        private final Deque<PoseData> poseStack = new ArrayDeque<>();

        private final float totalPoseTime;

        private float entityAge;

        private float maxTicks;
        private float tick;
        private float fullTime;
        private float ci;

        private boolean invalidated;

        private AnimationWrap(AnimationEntry animation, AnimationSystemInfo info, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, Function<String, AnimatableCube> anicubeRef, Collection<String> cubeNames , float age) {
            this.info = info;
            this.cuberef = cuberef;
            this.anicubeRef = anicubeRef;
            this.cubeNames = cubeNames;
            this.entityAge = age;
            this.entry = animation;
            this.poseStack.addAll(this.info.getPoseData(animation.getAnimation()));
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
                AnimationRunWrapper.CubeWrapper cubeWrapper = Objects.requireNonNull(this.cuberef.apply(partName));
                AnimatableCube cube = this.anicubeRef.apply(partName);

                float[] interpolatedRotation = this.getInterpRot(cubeWrapper);
                float[] interpolatedPosition = this.getInterpPos(cubeWrapper);

                float[] rotation = cube.getDefaultRotation();
                cube.addRotation(
                        interpolatedRotation[0] - rotation[0],
                        interpolatedRotation[1] - rotation[1],
                        interpolatedRotation[2] - rotation[2]
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
            this.tick += (age - this.entityAge) / timeModifier;

            //Make sure to catchup to correct render
            while (!this.invalidated && this.tick >= this.maxTicks && (!this.entry.hold || this.poseStack.size() > 1)) {
                this.poseStack.pop();
                if (this.poseStack.isEmpty()) {
                    if (this.entry.time == LOOP) {
                        this.poseStack.addAll(Lists.reverse(this.info.getPoseData(this.entry.animation)));
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
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(this.cuberef.apply(mapEntry.getKey()));
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

        public AnimationWrap copyAndApply(AnimationEntry entry) {
            AnimationWrap wrap = new AnimationWrap(entry, this.info, this.cuberef, this.anicubeRef, this.cubeNames, this.entityAge);

            wrap.poseStack.clear();
            wrap.poseStack.addAll(this.poseStack);

            wrap.maxTicks = this.maxTicks;
            wrap.tick = this.tick;
            wrap.fullTime = Math.min(wrap.fullTime, this.fullTime);

            wrap.ci = this.ci;

            return wrap;
        }

        public void onFinish() {
            for (String name : this.cubeNames) {
                AnimationRunWrapper.CubeWrapper cube = this.cuberef.apply(name);

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

        public float[] getInterpRot(AnimationRunWrapper.CubeWrapper cube) {
            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            return new float[] {
                    pr.x + (cr.x - pr.x) * this.ci,
                    pr.y + (cr.y - pr.y) * this.ci,
                    pr.z + (cr.z - pr.z) * this.ci
            };
        }

        public float[] getInterpPos(AnimationRunWrapper.CubeWrapper cube) {
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
        @Nullable
        private final AnimationEntry exitAnimation;

        public AnimationEntry(Animation animation) {
            this(animation, -1, animation.inertia(), animation.hold(), null);
        }

        public AnimationEntry(Animation animation, int time, boolean useInertia, boolean hold, @Nullable AnimationEntry andThen) {
            this.animation = animation;
            this.time = time;
            this.useInertia = useInertia;
            this.hold = hold;
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
}
