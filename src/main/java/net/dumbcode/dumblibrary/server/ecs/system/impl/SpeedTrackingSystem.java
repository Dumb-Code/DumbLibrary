package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SpeedTrackingComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpeedTrackingSystem implements EntitySystem {

    private Entity[] entities;
    private SpeedTrackingComponent[] speedComponents;

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.SPEED_TRACKING);
        this.entities = family.getEntities();
        this.speedComponents = family.populateBuffer(EntityComponentTypes.SPEED_TRACKING, this.speedComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            this.updateEntity(this.entities[i], this.speedComponents[i]);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        ClientWorld world = Minecraft.getInstance().level;
        if(world != null) {
            for (Entity entity : world.entitiesForRendering()) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(EntityComponentTypes.SPEED_TRACKING).ifPresent(comp -> this.updateEntity(entity, comp));
                }
            }
        }

    }

    private void updateEntity(Entity entity, SpeedTrackingComponent component) {
        component.setPreviousSpeed(component.getSpeed());
        double x = entity.position().x - entity.xo;
        double z = entity.position().z - entity.zo;
        float speed = MathHelper.sqrt(x * x + z * z);
        component.setSpeed(component.getSpeed() + (speed - component.getSpeed()));
    }
}
