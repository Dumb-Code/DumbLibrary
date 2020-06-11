package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.*;
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

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.IDLE_ACTION);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(EntityComponentTypes.IDLE_ACTION, this.components);
        this.animationComponents = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animationComponents);
        this.soundStorageComponents = family.populateBuffer(EntityComponentTypes.SOUND_STORAGE, this.soundStorageComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            IdleActionComponent component = this.components[i];
            AnimationComponent<?> animationComponent = this.animationComponents[i];
            SoundStorageComponent soundStorageComponent = this.soundStorageComponents[i];

            if(soundStorageComponent != null && entity.world.rand.nextInt(1000) < component.soundTicks++) {
                component.soundTicks -= 150;
                soundStorageComponent.getSound(ECSSounds.IDLE).ifPresent(e ->
                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, e, SoundCategory.AMBIENT, 1F, (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F + 1.0F)
                );
            }

            if(animationComponent != null && entity.world.rand.nextInt(1000) < component.animationTicks++) {
                ComponentAccess access = (ComponentAccess) entity;
                boolean sleeping = access.get(EntityComponentTypes.SLEEPING).map(SleepingComponent::isSleeping).orElse(false);
                boolean moving = entity.motionX*entity.motionX + entity.motionZ*entity.motionZ < 0.002;
                if(!sleeping && !moving) {
                    component.animationTicks -= 250;
                    animationComponent.playAnimation(access, component.idleAnimation, IDLE_CHANNEL);
                }
//                soundStorageComponent.getSound(ECSSounds.IDLE).ifPresent(e ->
//                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, e, SoundCategory.AMBIENT, 1F, (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F + 1.0F)
//                );
            }

        }
    }
}
