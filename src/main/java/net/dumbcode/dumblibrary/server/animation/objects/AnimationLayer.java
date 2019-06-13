package net.dumbcode.dumblibrary.server.animation.objects;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer<E extends Entity> {

    private final E entity;
    private final AnimationSystemInfo<E> info;
    private final Function<String, AnimationRunWrapper.CubeWrapper> cuberef;
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;
    private final boolean inertia;

    @Setter(AccessLevel.NONE)
    private AnimationWrap currentWrap;

    public AnimationLayer(E entity, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<E> info, boolean inertia) {
        this.entity = entity;
        this.info = info;
        this.cuberef = cuberef;
        this.anicubeRef = anicubeRef;
        this.currentWrap = new AnimationWrap(info.defaultAnimation(), 0);
        this.cubeNames = cubeNames;
        this.inertia = inertia;
    }

    public void animate(float age) {
        if (this.canAnimate()) {
            Animation animation = this.getAnimation();
            if (animation != this.currentWrap.animation) {
                this.setAnimation(animation, age);
            } else if (this.currentWrap.invalidated) {
                this.setAnimation(this.info.defaultAnimation(), age);
            }
            this.currentWrap.tick(age);
        }
    }

    public Animation getAnimation() {
        return this.info.getAnimation(this.entity);
    }


    public void setAnimation(Animation animation, float age) {
        if (this.info.getPoseData(animation).isEmpty()) {
            animation = this.info.defaultAnimation();
        }
        for (String name : this.cubeNames) {
            AnimationRunWrapper.CubeWrapper cube = this.cuberef.apply(name);

            float ci = this.currentWrap.ci;

            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            Vector3f np = cube.getRotationPoint();
            Vector3f pp = cube.getPrevRotationPoint();

            pr.x += (cr.x - pr.x) * ci;
            pr.y += (cr.y - pr.y) * ci;
            pr.z += (cr.z - pr.z) * ci;

            pp.x += (np.x - pp.x) * ci;
            pp.y += (np.y - pp.y) * ci;
            pp.z += (np.z - pp.z) * ci;

        }
        this.currentWrap = new AnimationWrap(animation, age);
    }

    public boolean canAnimate() {
        return true;
    }

    public boolean loop() {
        return false;
    }

    public class AnimationWrap {
        private final Animation animation;
        private final Deque<PoseData> poseStack = new LinkedList<>();

        private float entityAge;

        private float maxTicks;
        private float tick;
        private float ci;

        private boolean invalidated;

        public AnimationWrap(Animation animation, float age) {
            this.entityAge = age;
            this.animation = animation;
            this.poseStack.addAll(Lists.reverse(AnimationLayer.this.info.getPoseData(animation)));
            this.maxTicks = this.poseStack.peek().getTime();
            this.incrementVecs(false);
        }

        public void tick(float age) {
            if (this.invalidated) {
                return;
            }

            float perc = this.tick / this.maxTicks;

            this.ci = MathHelper.clamp(AnimationLayer.this.inertia && this.animation.inertia() ? (float) (Math.sin(Math.PI * (perc - 0.5D)) * 0.5D + 0.5D) : perc, 0, 1);

            for (String partName : AnimationLayer.this.cubeNames) {
                AnimationRunWrapper.CubeWrapper cubeWrapper = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(partName));
                AnimatableCube cube = AnimationLayer.this.anicubeRef.apply(partName);

                Vector3f cr = cubeWrapper.getRotation();
                Vector3f pr = cubeWrapper.getPrevRotation();

                float[] rotation = cube.getDefaultRotation();
                cube.addRotation(
                        (pr.x + (cr.x - pr.x) * this.ci) - rotation[0],
                        (pr.y + (cr.y - pr.y) * this.ci) - rotation[1],
                        (pr.z + (cr.z - pr.z) * this.ci) - rotation[2]
                );

                Vector3f np = cubeWrapper.getRotationPoint();
                Vector3f pp = cubeWrapper.getPrevRotationPoint();


                float[] positions = cube.getDefaultRotationPoint();
                cube.addRotationPoint(
                        (pp.x + (np.x - pp.x) * this.ci) - positions[0],
                        (pp.y + (np.y - pp.y) * this.ci) - positions[1],
                        (pp.z + (np.z - pp.z) * this.ci) - positions[2]
                );


            }
            this.tick += (age - this.entityAge);

            if (this.tick >= this.maxTicks && (!this.animation.hold() || this.poseStack.size() > 1)) {
                this.poseStack.pop();
                if (this.poseStack.isEmpty()) {
                    if (AnimationLayer.this.loop()) {
                        this.poseStack.addAll(Lists.reverse(AnimationLayer.this.info.getPoseData(this.animation)));
                    } else {
                        this.invalidated = true;
                    }
                }
                if (!this.invalidated) {
                    this.tick = 0;
                    this.maxTicks = this.poseStack.peek().getTime();
                    this.incrementVecs(true);
                }
            }
            this.entityAge = age;
        }

        private void incrementVecs(boolean updatePrevious) {
            for (Map.Entry<String, CubeReference> entry : this.poseStack.peek().getCubes().entrySet()) {
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(entry.getKey()));
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

                CubeReference next = entry.getValue();
                cr.x = next.getRotationX();
                cr.y = next.getRotationY();
                cr.z = next.getRotationZ();
                np.x = next.getRotationPointX();
                np.y = next.getRotationPointY();
                np.z = next.getRotationPointZ();
            }
        }
    }

    public interface AnimatableCube {
        float[] getDefaultRotationPoint();

        float[] getRotationPoint();

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
