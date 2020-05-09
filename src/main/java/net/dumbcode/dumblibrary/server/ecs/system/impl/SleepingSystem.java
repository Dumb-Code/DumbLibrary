package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class SleepingSystem implements EntitySystem {

    public static final int SLEEPING_CHANNEL = 62;

    private static final int RUNUPTO_TICKS = 1000;

    private Entity[] entities = new Entity[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];
    private EyesClosedComponent[] eyesClosedComponents = new EyesClosedComponent[0];
    private AnimationComponent[] animationComponents = new AnimationComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.SLEEPING);
        this.entities = family.getEntities();
        this.sleepingComponents = family.populateBuffer(EntityComponentTypes.SLEEPING, this.sleepingComponents);
        this.eyesClosedComponents = family.populateBuffer(EntityComponentTypes.EYES_CLOSED, this.eyesClosedComponents);
        this.animationComponents = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animationComponents);
    }

    @Override
    public void update(World world) {
        long time = world.getWorldTime() % 24000;
        for (int i = 0; i < this.entities.length; i++) {
            SleepingComponent component = this.sleepingComponents[i];
            AnimationComponent animationComponent = this.animationComponents[i];
            EyesClosedComponent eyesClosedComponent = this.eyesClosedComponents[i];

            //https://www.desmos.com/calculator/i7bpf2hi5w

            boolean shouldWake = time > component.getWakeupTime().getValue() && time < component.getSleepTime().getValue();
            if(component.isSleeping()) {
                 if(shouldWake) {
                     component.setSleeping(false);
                 }
                 if(eyesClosedComponent != null && eyesClosedComponent.getBlinkTicksLeft() <= 1) {
                     eyesClosedComponent.blink(20);
                 }
                if(animationComponent != null && !animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                    animationComponent.playAnimation((ComponentAccess) this.entities[i], new AnimationEntry(component.getSleepingAnimation()).withHold(true), SLEEPING_CHANNEL);
                }
            } else {
                if(!shouldWake) {
                    component.setSleeping(true);
                }
                if(animationComponent != null && animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                    animationComponent.stopAnimation(this.entities[i], SLEEPING_CHANNEL);
                }
            }
        }
    }
}
