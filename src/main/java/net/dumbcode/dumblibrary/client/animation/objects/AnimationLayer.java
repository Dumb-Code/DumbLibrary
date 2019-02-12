package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer<E extends Entity, N extends IStringSerializable> {

    private final E entity;
    private final N stage;
    private final AnimationSystemInfo<N, E> info;
    private final Function<String, AnimationRunWrapper.CubeWrapper> cuberef;
    private final Function<String, AnimatableCube> anicubeRef;
    private final Collection<String> cubeNames;
    private final boolean inertia;

    @Setter(AccessLevel.NONE)
    private AnimationWrap currentWrap;

    public AnimationLayer(E entity, N stage, Collection<String> cubeNames, Function<String, AnimatableCube> anicubeRef, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<N, E> info, boolean inertia) {
        this.entity = entity;
        this.stage = stage;
        this.info = info;
        this.cuberef = cuberef;
        this.anicubeRef = anicubeRef;
        this.currentWrap = new AnimationWrap(info.defaultAnimation(), 0);
        this.cubeNames = cubeNames;
        this.inertia = inertia;
    }

    public void animate(float age) {
        if (this.canAnimate()) {
            Animation<N> animation = this.getAnimation();
            if (animation != this.currentWrap.animation) {
                this.setAnimation(animation, age);
            } else if (this.currentWrap.invalidated) {
                this.setAnimation(this.info.defaultAnimation(), age);
            }
            this.currentWrap.tick(age);
        }
    }

    public Animation<N> getAnimation() {
        return this.info.getAnimation(this.entity);
    }


    public void setAnimation(Animation<N> animation, float age) {
        if (animation.getPoseData().isEmpty()) {
            animation = this.info.defaultAnimation();
        }
        for (String name : this.cubeNames) {
            AnimationRunWrapper.CubeWrapper cube = this.cuberef.apply(name);

            float ci = this.currentWrap.ci;

            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            Vector3f np = cube.getPosition();
            Vector3f pp = cube.getPrevPosition();

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
        private final Animation<N> animation;
        private final Stack<PoseData> poseStack = new Stack<>();

        private float entityAge;

        private float maxTicks;
        private float tick;
        private float ci;

        private boolean invalidated;

        public AnimationWrap(Animation<N> animation, float age) {
            this.entityAge = age;
            this.animation = animation;
            this.poseStack.addAll(Lists.reverse(animation.getPoseData().get(AnimationLayer.this.stage)));
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
                cube.setRotationX((pr.x + (cr.x - pr.x) * this.ci) - cube.getDefaultRotationX());
                cube.setRotationY((pr.y + (cr.y - pr.y) * this.ci) - cube.getDefaultRotationY());
                cube.setRotationZ((pr.z + (cr.z - pr.z) * this.ci) - cube.getDefaultRotationZ());

                Vector3f np = cubeWrapper.getPosition();
                Vector3f pp = cubeWrapper.getPrevPosition();

                cube.setPositionX((pp.x + (np.x - pp.x) * this.ci) - cube.getDefaultPositionX());
                cube.setPositionY((pp.y + (np.y - pp.y) * this.ci) - cube.getDefaultPositionY());
                cube.setPositionZ((pp.z + (np.z - pp.z) * this.ci) - cube.getDefaultPositionZ());

            }
            this.tick += (age - this.entityAge);

            if (this.tick >= this.maxTicks) {
                if (!this.animation.hold() || this.poseStack.size() > 1) {
                    this.poseStack.pop();
                    if (this.poseStack.isEmpty()) {
                        if (AnimationLayer.this.loop()) {
                            this.poseStack.addAll(Lists.reverse(this.animation.getPoseData().get(AnimationLayer.this.stage)));
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
            }
            this.entityAge = age;
        }

        private void incrementVecs(boolean updatePrevious) {
            for (Map.Entry<String, CubeReference> entry : this.poseStack.peek().getCubes().entrySet()) {
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(entry.getKey()));
                Vector3f cr = cube.getRotation();
                Vector3f pr = cube.getPrevRotation();

                Vector3f np = cube.getPosition();
                Vector3f pp = cube.getPrevPosition();

                if(updatePrevious) {
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
                np.x = next.getPositionX();
                np.y = next.getPositionY();
                np.z = next.getPositionZ();
            }
        }
    }

    public interface AnimatableCube {
        float getDefaultPositionX();
        float getDefaultPositionY();
        float getDefaultPositionZ();
        float getRotationPointX();
        float getRotationPointY();
        float getRotationPointZ();
        float getDefaultRotationX();
        float getDefaultRotationY();
        float getDefaultRotationZ();
        float getActualRotationX();
        float getActualRotationY();
        float getActualRotationZ();
        void setPositionX(float positionX);
        void setPositionY(float positionY);
        void setPositionZ(float positionZ);
        void setRotationX(float rotationX);
        void setRotationY(float rotationY);
        void setRotationZ(float rotationZ);

        @Nullable AnimatableCube getParent();

        default Vec3d getModelPos(Vec3d recurseValue) {
            double x = recurseValue.x;
            double y = recurseValue.y;
            double z = recurseValue.z;
            Point3d rendererPos = new Point3d(x, y, z);

            AnimatableCube parent = this.getParent();
            if (parent != null) {
                Matrix4d boxTranslate = new Matrix4d();
                Matrix4d boxRotateX = new Matrix4d();
                Matrix4d boxRotateY = new Matrix4d();
                Matrix4d boxRotateZ = new Matrix4d();
                boxTranslate.set(new Vector3d(parent.getRotationPointX()/16, -parent.getRotationPointY()/16, -parent.getRotationPointZ()/16));
                boxRotateX.rotX(parent.getActualRotationX());
                boxRotateY.rotY(-parent.getActualRotationY());
                boxRotateZ.rotZ(-parent.getActualRotationZ());

                boxRotateX.transform(rendererPos);
                boxRotateY.transform(rendererPos);
                boxRotateZ.transform(rendererPos);
                boxTranslate.transform(rendererPos);

                return parent.getModelPos(new Vec3d(rendererPos.getX(), rendererPos.getY(), rendererPos.getZ()));
            }
            return new Vec3d(rendererPos.getX(), rendererPos.getY(), rendererPos.getZ());
        }
    }

}
