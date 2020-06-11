package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.*;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SleepingSystem implements EntitySystem {

    public static final int SLEEPING_CHANNEL = 63;

    private static final int RUNUPTO_TICKS = 1000;

    private Entity[] entities = new Entity[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];
    private EyesClosedComponent[] eyesClosedComponents = new EyesClosedComponent[0];
    private AnimationComponent<?>[] animationComponents = new AnimationComponent[0];
    private SoundStorageComponent[] soundStorageComponents = new SoundStorageComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.SLEEPING);
        this.entities = family.getEntities();
        this.sleepingComponents = family.populateBuffer(EntityComponentTypes.SLEEPING, this.sleepingComponents);
        this.eyesClosedComponents = family.populateBuffer(EntityComponentTypes.EYES_CLOSED, this.eyesClosedComponents);
        this.animationComponents = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animationComponents);
        this.soundStorageComponents = family.populateBuffer(EntityComponentTypes.SOUND_STORAGE, this.soundStorageComponents);
    }

    @Override
    public void update(World world) {
        long time = world.getWorldTime() % 24000;
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            SleepingComponent component = this.sleepingComponents[i];
            EyesClosedComponent eyesClosedComponent = this.eyesClosedComponents[i];
            SoundStorageComponent soundStorageComponent = this.soundStorageComponents[i];

            this.ensureAnimation(entity, component, this.animationComponents[i]);

            //TODO: add support for waking up before 0
            //https://www.desmos.com/calculator/9ac2nhfp16
            boolean shouldWake = time > component.getWakeupTime().getValue() && time < component.getSleepTime().getValue();
            component.setSleeping(component.isSleeping());
            if(component.isSleeping()) {
                if(shouldWake) {
                    component.setSleeping(false);
                }

                if(eyesClosedComponent != null && eyesClosedComponent.getBlinkTicksLeft() <= 1) {
                     eyesClosedComponent.blink(20);
                 }

                //TODO-stream: remove
                if(entity instanceof EntityCreature) {
                    ((EntityCreature) entity).getNavigator().setPath(null, 0);
                }

            } else {
                if(!shouldWake) {
                    component.setSleeping(true);
                }
            }

            if(soundStorageComponent != null && entity.world.rand.nextInt(500) < component.snoringTicks++) {
                component.snoringTicks -= 75;
                soundStorageComponent.getSound(ECSSounds.SNORING).ifPresent(e ->
                    entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, e, SoundCategory.AMBIENT, 1F, (entity.world.rand.nextFloat() - entity.world.rand.nextFloat()) * 0.2F + 1.0F)
                );
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        World world = Minecraft.getMinecraft().world;
        if(world != null) {
            for (Entity entity : world.loadedEntityList) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(EntityComponentTypes.SLEEPING).ifPresent(comp -> this.ensureAnimation(entity, comp, ((ComponentAccess) entity).getOrNull(EntityComponentTypes.ANIMATION)));
                }
            }
        }

    }

    //TODO-stream: maybe every few ticks the animations that are playing should be synced.
    //or maybe if an entity can't be found or the animation player isn't ready it ticks untill it is then runs the command.
    private void ensureAnimation(Entity entity, SleepingComponent component, AnimationComponent<?> animationComponent) {
        if(component.isSleeping()) {
            if(animationComponent != null && !animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                animationComponent.playAnimation((ComponentAccess) entity, component.getSleepingAnimation().createEntry().withHold(true).withSpeed(1.5F), SLEEPING_CHANNEL);
            }
        } else if(animationComponent != null && animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
            animationComponent.stopAnimation(entity, SLEEPING_CHANNEL);
        }
    }
}
