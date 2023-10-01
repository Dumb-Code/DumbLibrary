package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SoundStorageComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SleepingAnimationSystem implements EntitySystem {

    public static final int SLEEPING_CHANNEL = 63;

    private static final int RUNUPTO_TICKS = 1000;

    private Entity[] entities = new Entity[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];
    private AnimationComponent[] animationComponents = new AnimationComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.SLEEPING);
        this.entities = family.getEntities();
        this.sleepingComponents = family.populateBuffer(EntityComponentTypes.SLEEPING, this.sleepingComponents);
        this.animationComponents = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animationComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            this.ensureAnimation(entity, this.sleepingComponents[i], this.animationComponents[i]);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        ClientWorld world = Minecraft.getInstance().level;
        if(world != null) {
            for (Entity entity : world.entitiesForRendering()) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(EntityComponentTypes.SLEEPING).ifPresent(comp -> this.ensureAnimation(entity, comp, ((ComponentAccess) entity).getOrNull(EntityComponentTypes.ANIMATION)));
                }
            }
        }

    }

    //TODO-stream: maybe every few ticks the animations that are playing should be synced.
    //or maybe if an entity can't be found or the animation player isn't ready it ticks untill it is then runs the command.
    private void ensureAnimation(Entity entity, SleepingComponent component, AnimationComponent animationComponent) {
        if(component.isSleeping()) {
            if(animationComponent != null && !animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
                animationComponent.playAnimation(component.getSleepingAnimation(), SLEEPING_CHANNEL).holdForever().withSpeed(1.5F);
            }
        } else if(animationComponent != null && animationComponent.isChannelActive(SLEEPING_CHANNEL)) {
            animationComponent.stopAnimation(SLEEPING_CHANNEL);
        }
    }
}
