package net.dumbcode.dumblibrary.client.animation.objects;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.AnimationInfo;
import net.dumbcode.dumblibrary.client.animation.PoseHandler;
import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.server.animation.Animation;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The Animation Pass. Directly animates the entity.
 * @param <T> the entity type
 */
public class AnimationPass<T extends EntityAnimatable> {

    protected final Map<Animation,List<PoseHandler.PoseData>> animations;
    protected final Map<String, Map<String, CubeReference>> poses;
    protected final boolean useInertia;

    //~~~~~~~~~~~
    protected Map<String, Vector3f> rotationIncrements = Maps.newHashMap();
    protected Map<String, Vector3f> positionIncrements = Maps.newHashMap();

    protected Map<String, Vector3f> prevRotationIncrements = Maps.newHashMap();
    private Map<String, Vector3f> prevPositionIncrements = Maps.newHashMap();
    //~~~~~~~~~~~

    protected int poseCount;
    protected int poseIndex;
    protected float poseLength;

    protected float animationTick;
    protected float prevTicks;

    protected Function<String, AdvancedModelRenderer> partFunc;
    protected Map<String, CubeReference> currentPose = Maps.newHashMap();

    protected Animation animation;

    protected float inertiaFactor;
    protected float limbSwing;
    protected float limbSwingAmount;

    protected final Animation defaultAnimation;
    protected final Function<Animation, AnimationInfo> animationInfoGetter;

    /**
     *
     * @param defaultAnimation the default animation for the entity. Should be a idle animation, or something similar
     * @param animations The map of animations to all the PoseData
     * @param poses The map of model names to a map of cube names to cube references. Used to get the position/rotation a cube should be in
     * @param animationInfoGetter a function to get the animation information from an animation
     * @param useInertia should inertia tweens be used
     */
    public AnimationPass(Animation defaultAnimation, Map<Animation, List<PoseHandler.PoseData>> animations, Map<String, Map<String, CubeReference>> poses, Function<Animation, AnimationInfo> animationInfoGetter, boolean useInertia) {
        this.defaultAnimation = defaultAnimation;
        this.animations = animations;
        this.poses = poses;
        this.animationInfoGetter = animationInfoGetter;
        this.useInertia = useInertia;
    }

    public void init(TabulaModel model, T entity) {
        this.partFunc = model::getCube;
        this.animation = this.getRequestedAnimation(entity);
        this.initPoseModel();
        this.initAnimation(this.getRequestedAnimation(entity));
        this.initAnimationTicks(entity);

        this.prevTicks = 0.0F;
        this.initIncrements(entity);
        this.performAnimations(entity, 0.0F, 0.0F, 0.0F);
    }

    private void initPoseModel() {
        List<PoseHandler.PoseData> pose = this.animations.get(this.animation);
        if (pose != null) {
            this.poseCount = pose.size();
            this.poseIndex = 0;
            this.setCurrentPose(this.poses.getOrDefault(pose.get(this.poseIndex).getModelName(), Maps.newHashMap()));
        }
    }

    private void initIncrements(T entity) {
        float animationDegree = this.getAnimationDegree(entity);
        for (String name : this.currentPose.keySet()) {
            AdvancedModelRenderer part = this.partFunc.apply(name);
            CubeReference nextPose = this.currentPose.get(name);

            Vector3f rotationIncrements = this.rotationIncrements.computeIfAbsent(name, n -> new Vector3f());
            Vector3f positionIncrements = this.positionIncrements.computeIfAbsent(name, n -> new Vector3f());

            Vector3f prevRotationIncrements = this.prevRotationIncrements.computeIfAbsent(name, n -> new Vector3f());
            Vector3f prevPositionIncrements = this.prevPositionIncrements.computeIfAbsent(name, n -> new Vector3f());

            rotationIncrements.x = (nextPose.getRotationX() - (part.defaultRotationX + prevRotationIncrements.x)) * animationDegree;
            rotationIncrements.y = (nextPose.getRotationY() - (part.defaultRotationY + prevRotationIncrements.y)) * animationDegree;
            rotationIncrements.z = (nextPose.getRotationZ() - (part.defaultRotationZ + prevRotationIncrements.z)) * animationDegree;

            positionIncrements.x = (nextPose.getPositionX() - (part.defaultPositionX + prevPositionIncrements.x)) * animationDegree;
            positionIncrements.y = (nextPose.getPositionY() - (part.defaultPositionY + prevPositionIncrements.y)) * animationDegree;
            positionIncrements.z = (nextPose.getPositionZ() - (part.defaultPositionZ + prevPositionIncrements.z)) * animationDegree;
        }
    }

    private void applyRotations(String partName) {
        AdvancedModelRenderer part = this.partFunc.apply(partName);

        Vector3f current = this.rotationIncrements.computeIfAbsent(partName, n -> new Vector3f());
        Vector3f previous = this.prevRotationIncrements.computeIfAbsent(partName, n -> new Vector3f());

        part.rotateAngleX += (current.x * this.inertiaFactor + previous.x);
        part.rotateAngleY += (current.y * this.inertiaFactor + previous.y);
        part.rotateAngleZ += (current.z * this.inertiaFactor + previous.z);
    }

    private void applyTranslations(String partName) {
        AdvancedModelRenderer part = this.partFunc.apply(partName);

        Vector3f current = this.rotationIncrements.computeIfAbsent(partName, n -> new Vector3f());
        Vector3f previous = this.prevRotationIncrements.computeIfAbsent(partName, n -> new Vector3f());

        part.rotationPointX += (current.x * this.inertiaFactor + previous.x);
        part.rotationPointY += (current.y * this.inertiaFactor + previous.y);
        part.rotationPointZ += (current.z * this.inertiaFactor + previous.z);
    }

    private void initAnimation(Animation animation) {
        this.animation = animation;

        if (this.animations.get(animation) == null) {
            this.animation = this.defaultAnimation;
        }
    }

    private float calculateInertiaFactor() {
        float inertiaFactor = this.animationTick / this.poseLength;

        if (this.useInertia && this.animationInfoGetter.apply(this.animation).useInertia()) {
            inertiaFactor = (float) (Math.sin(Math.PI * (inertiaFactor - 0.5D)) * 0.5D + 0.5D);
        }

        return Math.min(1.0F, Math.max(0.0F, inertiaFactor));
    }

    void performAnimations(T entity, float limbSwing, float limbSwingAmount, float ticks) {
        Animation requestedAnimation = this.getRequestedAnimation(entity);

        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;

        if (requestedAnimation != this.animation) {
            this.setAnimation(entity, requestedAnimation);
        }

        if (this.poseIndex >= this.poseCount) {
            this.poseIndex = this.poseCount - 1;
        }

        this.inertiaFactor = this.calculateInertiaFactor();

        for (String name : this.currentPose.keySet()) {
            this.applyRotations(name);
            this.applyTranslations(name);
        }

        if (this.updateAnimationTick(entity, ticks)) {
            this.onPoseFinish(entity, ticks);
        }

        this.prevTicks = ticks;
    }

    private boolean updateAnimationTick(T entity,  float ticks) {
        float incrementAmount = (ticks - this.prevTicks) * this.getAnimationSpeed(entity);
        if (this.animationTick < 0.0F) {
            this.animationTick = 0.0F;
        }
        if (!this.animationInfoGetter.apply(this.animation).shouldHold() || this.poseIndex < this.poseCount) {
            this.animationTick += incrementAmount;

            if (this.animationTick >= this.poseLength) {
                this.animationTick = this.poseLength;

                return true;
            }
            return false;
        } else {
            if (this.animationTick < this.poseLength) {
                this.animationTick += incrementAmount;

                if (this.animationTick >= this.poseLength) {
                    this.animationTick = this.poseLength;
                }
            } else {
                this.animationTick = this.poseLength;
            }

            return false;
        }
    }

    private void initAnimationTicks(T entity) {
        this.startAnimation(entity);
        if (this.animationInfoGetter.apply(this.animation).shouldHold()) {
            this.poseIndex = this.poseCount - 1;
            this.animationTick = this.animations.get(this.animation).get(this.poseIndex).getTime();
        } else {
            this.animationTick = 0;
        }
    }

    private void setCurrentPose(Map<String, CubeReference> currentPose) {
        this.currentPose = currentPose;
    }

    private void startAnimation(T entity) {
        List<PoseHandler.PoseData> pose = this.animations.get(this.animation);
        if (pose != null) {
            String model = this.getModelData().getModelName();
            this.setCurrentPose(this.poses.get(model));
            this.poseLength = Math.max(1, pose.get(this.poseIndex).getTime());
            this.animationTick = 0;

            this.initIncrements(entity);
        }
    }

    private void setPose(T entity, float ticks) {
        this.setCurrentPose(this.poses.get(this.getModelData().getModelName()));
        this.poseLength = this.animations.get(this.animation).get(this.poseIndex).getTime();
        this.animationTick = 0;
        this.prevTicks = ticks;
        this.initIncrements(entity);
    }

    private void onPoseFinish(T entity, float ticks) {
        if (this.incrementPose()) {
            this.setAnimation(entity, this.isEntityAnimationDependent() ? this.defaultAnimation : this.getRequestedAnimation(entity));
        } else {
            this.updatePreviousPose();
        }
        this.setPose(entity, ticks);
    }

    private boolean incrementPose() {
        this.poseIndex++;
        if (this.poseIndex >= this.poseCount) {
            if (animation != null && this.animationInfoGetter.apply(this.animation).shouldHold()) {
                this.poseIndex = this.poseCount - 1;
            } else {
                this.poseIndex = 0;
                return true;
            }
        }
        return false;
    }

    private void setAnimation(T entity, Animation requestedAnimation) {
        this.updatePreviousPose();

        if (this.animations.get(requestedAnimation) != null && !(this.animation != this.defaultAnimation && this.animation == requestedAnimation && !this.isLooping())) {
            this.animation = requestedAnimation;
        } else {
            this.animation = this.defaultAnimation;
        }

        this.poseIndex = 0;
        this.setCurrentPose(this.poses.get(this.getModelData().getModelName()));

        this.startAnimation(entity);
    }

    private void updatePreviousPose() {
        for (String partName : this.prevRotationIncrements.keySet()) {
            this.prevRotationIncrements.computeIfAbsent(partName, n -> new Vector3f()).x += this.rotationIncrements.computeIfAbsent(partName, n -> new Vector3f()).x * this.inertiaFactor;
            this.prevRotationIncrements.get(partName).y += this.rotationIncrements.get(partName).y * this.inertiaFactor;
            this.prevRotationIncrements.get(partName).z += this.rotationIncrements.get(partName).z * this.inertiaFactor;

            this.prevPositionIncrements.computeIfAbsent(partName, n -> new Vector3f()).x += this.positionIncrements.computeIfAbsent(partName, n -> new Vector3f()).x * this.inertiaFactor;
            this.prevPositionIncrements.get(partName).y += this.positionIncrements.get(partName).y * this.inertiaFactor;
            this.prevPositionIncrements.get(partName).z += this.positionIncrements.get(partName).z * this.inertiaFactor;
        }
    }

    public boolean shouldRun(T entity) {
        return true;
    }

    private PoseHandler.PoseData getModelData() {
        return this.animations.get(this.animation).get(this.poseIndex);
    }

    protected float getAnimationSpeed(T entity) {
        return 1.0F;
    }

    protected float getAnimationDegree(T entity) {
        return 1.0F;
    }

    protected Animation getRequestedAnimation(T entity) {
        return entity.getAnimation();
    }

    protected boolean isEntityAnimationDependent() {
        return true;
    }

    protected boolean isLooping() {
        return false;
    }
}
