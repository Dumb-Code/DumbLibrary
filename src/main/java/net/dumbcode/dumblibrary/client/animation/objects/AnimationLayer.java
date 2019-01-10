package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.util.math.MathHelper;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer<T extends AnimatedEntity> {

    private final T entity;
    private final TabulaModel model;
    @Setter(AccessLevel.NONE)
    private AnimationWrap currentWrap;
    private final Function<String, AnimationRunWrapper.CubeWrapper> cuberef;
    private final List<String> cubeNames = Lists.newArrayList();

    private final boolean inertia;

    public AnimationLayer(T entity, TabulaModel model, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, Animation defaultAnimation, boolean inertia) {
        this.entity = entity;
        this.model = model;
        this.cuberef = cuberef;
        this.currentWrap = new AnimationWrap(defaultAnimation, 0);
        this.inertia = inertia;

        this.cubeNames.addAll(model.getCubes().keySet());
    }

    public void animate(float age) {
        if (this.canAnimate()) {
            Animation animation = this.getAnimation();
            if (animation != this.currentWrap.animation) {
                this.setAnimation(animation, age);
            } else if (this.currentWrap.invalidated) {
                this.setAnimation(this.entity.getInfo().defaultAnimation(), age);
            }
            this.currentWrap.tick(age);
        }
    }

    public Animation getAnimation() {
        return this.entity.getAnimation();
    }


    public void setAnimation(Animation animation, float age) {
        if (animation.getPoseData().isEmpty()) {
            animation = this.entity.getInfo().defaultAnimation();
        }
        for (String name : this.cubeNames) {
            AnimationRunWrapper.CubeWrapper cube = this.cuberef.apply(name);

            float ci = this.currentWrap.ci;

            Vector3f cr = cube.getRotation();
            Vector3f pr = cube.getPrevRotation();

            Vector3f cp = cube.getPosition();
            Vector3f pp = cube.getPosition();

            pr.x += (cr.x - pr.x) * ci;
            pr.y += (cr.y - pr.y) * ci;
            pr.z += (cr.z - pr.z) * ci;

            pp.x += (cp.x - pp.x) * ci;
            pp.y += (cp.y - pp.y) * ci;
            pp.z += (cp.z - pp.z) * ci;

        }
        this.currentWrap = new AnimationWrap(animation, age);
    }

    public boolean canAnimate() {
        return true;
    }

    public class AnimationWrap {
        private final Animation animation;
        private final Stack<PoseData> poseStack = new Stack<>();

        private float entityAge;

        private float maxTicks;
        private float tick;
        private float ci;

        private boolean invalidated;

        public AnimationWrap(Animation animation, float age) {
            this.entityAge = age;
            this.animation = animation;
            this.poseStack.addAll(Lists.reverse(animation.getPoseData()));
            this.maxTicks = this.poseStack.peek().getTime();
            this.incrementVecs();
        }

        public void tick(float age) {
            if (this.invalidated) {
                return;
            }

            float perc = this.tick / this.maxTicks;
            this.ci = MathHelper.clamp(AnimationLayer.this.inertia && this.animation.inertia() ? (float) (Math.sin(Math.PI * (perc - 0.5D)) * 0.5D + 0.5D) : perc, 0, 1);

            for (String partName : AnimationLayer.this.cubeNames) {
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(partName));
                AdvancedModelRenderer part = AnimationLayer.this.model.getCube(partName);

                Vector3f cr = cube.getRotation();
                Vector3f pr = cube.getPrevRotation();
                part.rotateAngleX = pr.x + (cr.x - pr.x) * this.ci;
                part.rotateAngleY = pr.y + (cr.y - pr.y) * this.ci;
                part.rotateAngleZ = pr.z + (cr.z - pr.z) * this.ci;


                Vector3f cp = cube.getPosition();
                Vector3f pp = cube.getPrevPosition();

                part.rotationPointX = pp.x + (cp.x - pp.x) * this.ci;
                part.rotationPointY = pp.y + (cp.y - pp.y) * this.ci;
                part.rotationPointZ = pp.z + (cp.z - pp.z) * this.ci;

            }
            this.tick += (age - this.entityAge);

            if (this.tick >= this.maxTicks) {
                if (!this.animation.hold() || this.poseStack.size() > 1) {
                    this.poseStack.pop();
                    if (this.poseStack.isEmpty()) {
                        if (this.animation.hold()) {
                            this.poseStack.addAll(Lists.reverse(this.animation.getPoseData()));
                        } else {
                            this.invalidated = true;
                        }
                    }
                    if (!this.invalidated) {
                        this.tick = 0;
                        this.maxTicks = this.poseStack.peek().getTime();
//                        this.setPrevious();

                        this.incrementVecs();
                    }
                }
            }
            this.entityAge = age;
        }

        private void incrementVecs() {
            for (Map.Entry<String, CubeReference> entry : this.poseStack.peek().getCubes().entrySet()) {
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(entry.getKey()));
                Vector3f cr = cube.getRotation();
                Vector3f pr = cube.getPrevRotation();

                Vector3f cp = cube.getPosition();
                Vector3f pp = cube.getPrevPosition();

                pr.x = cr.x;
                pr.y = cr.y;
                pr.z = cr.z;

                pp.x = cp.x;
                pp.y = cp.y;
                pp.z = cp.z;

                CubeReference next = entry.getValue();
                cr.x = next.getRotationX();
                cr.y = next.getRotationY();
                cr.z = next.getRotationZ();
                cp.x = next.getPositionX();
                cp.y = next.getPositionY();
                cp.z = next.getPositionZ();
            }

        }

        private void setCubeWrapper() {
            for (Map.Entry<String, CubeReference> entry : this.poseStack.peek().getCubes().entrySet()) {
                String partName = entry.getKey();
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(partName));
                AdvancedModelRenderer part = AnimationLayer.this.model.getCube(partName);
                CubeReference nextPose = entry.getValue();

                Vector3f cr = cube.getRotation();
                Vector3f cp = cube.getPosition();

                Vector3f pr = cube.getPrevRotation();
                Vector3f pp = cube.getPrevPosition();

                cr.x = nextPose.getRotationX() - (part.defaultRotationX + pr.x);
                cr.y = nextPose.getRotationY() - (part.defaultRotationY + pr.y);
                cr.z = nextPose.getRotationZ() - (part.defaultRotationZ + pr.z);

                cp.x = nextPose.getPositionX() - (part.defaultPositionX + pp.x);
                cp.y = nextPose.getPositionY() - (part.defaultPositionY + pp.y);
                cp.z = nextPose.getPositionZ() - (part.defaultPositionZ + pp.z);
            }
        }

        private void setPrevious() {
            for (String partName : AnimationLayer.this.cubeNames) {
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(partName));
                Vector3f cr = cube.getRotation();
                Vector3f pr = cube.getPrevRotation();

                Vector3f cp = cube.getPosition();
                Vector3f pp = cube.getPrevPosition();

                pr.x += cr.x * this.ci;
                pr.y += cr.y * this.ci;
                pr.z += cr.z * this.ci;

                pp.x += cp.x * this.ci;
                pp.y += cp.y * this.ci;
                pp.z += cp.z * this.ci;

            }
        }
    }

}
