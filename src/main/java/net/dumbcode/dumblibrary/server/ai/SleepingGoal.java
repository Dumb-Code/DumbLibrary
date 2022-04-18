package net.dumbcode.dumblibrary.server.ai;

import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SoundStorageComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

import java.util.OptionalDouble;

public class SleepingGoal extends EntityGoal {

    private final CreatureEntity mob;
    private final SleepingComponent component;
    private final EyesClosedComponent eyesClosedComponent;
    private final SoundStorageComponent soundStorageComponent;

    public SleepingGoal(GoalManager manager, CreatureEntity mob, SleepingComponent component, EyesClosedComponent eyesClosedComponent, SoundStorageComponent soundStorageComponent) {
        super(manager);
        this.mob = mob;
        this.component = component;
        this.eyesClosedComponent = eyesClosedComponent;
        this.soundStorageComponent = soundStorageComponent;
    }

    @Override
    protected void tick() {
        if(this.eyesClosedComponent != null && this.eyesClosedComponent.getBlinkTicksLeft() <= 1) {
            this.eyesClosedComponent.blink(20);
        }

        //TODO-stream: remove
        this.mob.getNavigation().moveTo((Path) null, 0);

        if(this.soundStorageComponent != null && RANDOM.nextInt(500) < this.component.snoringTicks++) {
            this.component.snoringTicks -= 75;
            Vector3d d = this.mob.position();
            this.soundStorageComponent.getSound(ECSSounds.SNORING).ifPresent(e ->
                this.mob.level.playSound(null, d.x, d.y, d.z, e, SoundCategory.AMBIENT, 1F, (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2F + 1.0F)
            );
        }

        if(!this.shouldSleep()) {
            this.finish();
        }
    }

    @Override
    public boolean onStarted() {
        //Animations are handled by SleepingAnimationSystem (for now)
        //In the future, find a way to serialize the entire animation wrap to the network
        this.component.setSleeping(true);
        return true;
    }

    @Override
    public void onFinished() {
        this.component.setSleeping(false);
    }

    @Override
    protected OptionalDouble getImportance() {
        return this.shouldSleep() ? OptionalDouble.of(100) : OptionalDouble.empty() ;
    }

    private boolean shouldSleep() {
        //TODO: add support for waking up before 0
        //https://www.desmos.com/calculator/9ac2nhfp16
        long time = this.mob.level.dayTime() % 24000;
        return !(time > this.component.getWakeupTime().getValue() && time < this.component.getSleepTime().getValue());
    }
}
