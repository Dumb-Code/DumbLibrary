package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.IdleActionComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SoundStorageComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class IdleActionSystem implements EntitySystem {

    public static int IDLE_CHANNEL = 64;

    private Entity[] entities = new Entity[0];
    private IdleActionComponent[] components = new IdleActionComponent[0];
    private AnimationComponent<?>[] animationComponents = new AnimationComponent[0];
    private SoundStorageComponent[] soundStorageComponents = new SoundStorageComponent[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.IDLE_ACTION);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(EntityComponentTypes.IDLE_ACTION, this.components);
        this.animationComponents = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animationComponents);
        this.soundStorageComponents = family.populateBuffer(EntityComponentTypes.SOUND_STORAGE, this.soundStorageComponents);
        this.sleepingComponents = family.populateBuffer(EntityComponentTypes.SLEEPING, this.sleepingComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            IdleActionComponent component = this.components[i];
            AnimationComponent<?> animationComponent = this.animationComponents[i];
            SoundStorageComponent soundStorageComponent = this.soundStorageComponents[i];
            SleepingComponent sleep = this.sleepingComponents[i];

            if(soundStorageComponent != null && (sleep == null || !sleep.isSleeping()) && entity.world.rand.nextInt(1000) < component.soundTicks++) {
                component.soundTicks -= 150;
                soundStorageComponent.getSound(ECSSounds.IDLE).ifPresent(e ->
                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, e, SoundCategory.AMBIENT, 1F, (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F + 1.0F)
                );
            }

            if(animationComponent != null && (sleep == null || !sleep.isSleeping()) && entity.world.rand.nextInt(500) < component.animationTicks++) {
                ComponentAccess access = (ComponentAccess) entity;
                boolean sleeping = access.get(EntityComponentTypes.SLEEPING).map(SleepingComponent::isSleeping).orElse(false);
                boolean still = entity.motionX*entity.motionX + entity.motionZ*entity.motionZ < 0.002;
                boolean running = entity.motionX*entity.motionX + entity.motionZ*entity.motionZ > 0.03;

                if(!sleeping && still && world.rand.nextBoolean()) {
                    component.animationTicks -= 150;
                    animationComponent.playAnimation(access, world.rand.nextFloat() < 0.3 ? component.sittingAnimation.createEntry().withHold(true) : component.idleAnimation.createEntry(), IDLE_CHANNEL);
                } else if(!sleeping && !component.movementAnimation.isEmpty() && !running) {
                    component.animationTicks -= 30;
                    animationComponent.playAnimation(access, component.movementAnimation.get(world.rand.nextInt(component.movementAnimation.size())), IDLE_CHANNEL);
                } else {
                    animationComponent.stopAnimation(entity, IDLE_CHANNEL);
                }
            }

        }
    }
}
