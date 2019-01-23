package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;

@Getter
@Setter
public class AnimationLayer<E extends Entity, N extends IStringSerializable> {

    private final E entity;
    private final N stage;
    private final TabulaModel model;
    private final AnimationSystemInfo<N, E> info;
    private final Function<String, AnimationRunWrapper.CubeWrapper> cuberef;
    private final List<String> cubeNames = Lists.newArrayList();
    private final boolean inertia;

    @Setter(AccessLevel.NONE)
    private AnimationWrap currentWrap;

    public AnimationLayer(E entity, N stage, TabulaModel model, Function<String, AnimationRunWrapper.CubeWrapper> cuberef, AnimationSystemInfo<N, E> info, boolean inertia) {
        this.entity = entity;
        this.stage = stage;
        this.model = model;
        this.info = info;
        this.cuberef = cuberef;
        this.currentWrap = new AnimationWrap(info.defaultAnimation(), 0);
        this.inertia = inertia;

        this.cubeNames.addAll(model.getCubes().keySet());
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
                AnimationRunWrapper.CubeWrapper cube = Objects.requireNonNull(AnimationLayer.this.cuberef.apply(partName));
                AdvancedModelRenderer part = AnimationLayer.this.model.getCube(partName);

                Vector3f cr = cube.getRotation();
                Vector3f pr = cube.getPrevRotation();
                part.rotateAngleX += (pr.x + (cr.x - pr.x) * this.ci) - part.defaultRotationX;
                part.rotateAngleY += (pr.y + (cr.y - pr.y) * this.ci) - part.defaultRotationY;
                part.rotateAngleZ += (pr.z + (cr.z - pr.z) * this.ci) - part.defaultRotationZ;


                Vector3f np = cube.getPosition();
                Vector3f pp = cube.getPrevPosition();

                part.rotationPointX += (pp.x + (np.x - pp.x) * this.ci) - part.defaultPositionX;
                part.rotationPointY += (pp.y + (np.y - pp.y) * this.ci) - part.defaultPositionY;
                part.rotationPointZ += (pp.z + (np.z - pp.z) * this.ci) - part.defaultPositionZ;

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

}
