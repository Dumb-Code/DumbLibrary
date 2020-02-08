package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationLayer;
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

    private long previousWorldTime;

    private Entity[] entities = new Entity[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];
    private EyesClosedComponent[] eyesClosedComponents = new EyesClosedComponent[0];
    private AnimationComponent[] animationComponents = new AnimationComponent[0];

    public SleepingSystem(long worldTime) {
        this.previousWorldTime = worldTime;
    }

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
        int timeDiff = (int) Math.max(world.getWorldTime() - this.previousWorldTime, 1); //the `/time` command can reset world time, so worldTime - previousWorldTime would be negative. We can't have this.
        for (int i = 0; i < this.entities.length; i++) {
            SleepingComponent component = this.sleepingComponents[i];
            AnimationComponent animationComponent = this.animationComponents[i];

            if (component.getSleepingTicksLeft() > 0) { //Sleeping
                component.setSleepingTicksLeft(component.getSleepingTicksLeft() - timeDiff);
                component.setTiredness(component.getTiredness() - component.getTirednessLossPerTickSleeping().getValue() * timeDiff);
                if(animationComponent != null && !animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                    animationComponent.playAnimation((ComponentAccess) this.entities[i], new AnimationLayer.AnimationEntry(component.getSleepingAnimation()).withHold(true), SLEEPING_CHANNEL);
                }
                if (this.eyesClosedComponents[i] != null) {
                    this.eyesClosedComponents[i].blink(5);
                }
            } else {
                component.setTiredness(component.getTiredness() + timeDiff);
                if(animationComponent != null && animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                    animationComponent.stopAnimation(this.entities[i], SLEEPING_CHANNEL);
                }

                if(this.entities[i].world.rand.nextFloat() < component.calculateChanceToSleep()) {
                    //To go sleep
                    component.setSleepingTicksLeft((int) (component.getTiredness() / component.getTirednessLossPerTickSleeping().getValue()));
                }
            }
        }
        this.previousWorldTime = world.getWorldTime();
    }
}
