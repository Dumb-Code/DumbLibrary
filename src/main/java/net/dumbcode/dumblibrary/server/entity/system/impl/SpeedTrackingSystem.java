package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.SpeedTrackingComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum SpeedTrackingSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities;
    private SpeedTrackingComponent[] speedComponents;

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.SPEED_TRACKING);
        this.entities = family.getEntities();
        this.speedComponents = family.populateBuffer(EntityComponentTypes.SPEED_TRACKING, this.speedComponents);
    }

    @Override
    public void update() {
        for (int i = 0; i < this.entities.length; i++) {
            this.updateEntity(this.entities[i], this.speedComponents[i]);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientWorldTick(TickEvent.ClientTickEvent event) {
        World world = Minecraft.getMinecraft().world;
        if(world != null) {
            for (Entity entity : world.loadedEntityList) {
                if(entity instanceof ComponentAccess) {
                    ((ComponentAccess) entity).get(EntityComponentTypes.SPEED_TRACKING).ifPresent(comp -> this.updateEntity(entity, comp));
                }
            }
        }

    }

    private void updateEntity(Entity entity, SpeedTrackingComponent component) {
        component.setPreviousSpeed(component.getSpeed());
        double x = entity.motionX;
        double z = entity.motionZ;
        float speed = MathHelper.sqrt(x * x + z * z);

        component.setSpeed(component.getSpeed() + (speed - component.getSpeed()) * 0.1F);
    }
}
