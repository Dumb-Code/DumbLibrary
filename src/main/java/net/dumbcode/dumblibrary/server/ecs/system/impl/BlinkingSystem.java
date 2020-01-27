package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.BlinkingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public enum BlinkingSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private EyesClosedComponent[] eyesClosed = new EyesClosedComponent[0];
    private BlinkingComponent[] blinkingComponents = new BlinkingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.EYES_CLOSED, EntityComponentTypes.BLINKING);
        this.entities = family.getEntities();
        this.eyesClosed = family.populateBuffer(EntityComponentTypes.EYES_CLOSED, this.eyesClosed);
        this.blinkingComponents = family.populateBuffer(EntityComponentTypes.BLINKING, this.blinkingComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            int ticksExisted = this.entities[i].ticksExisted;
            BlinkingComponent component = this.blinkingComponents[i];
            if(ticksExisted % (component.getTickTimeOpen() + component.getTickTimeClose()) == component.getTickTimeOpen()) {
                this.eyesClosed[i].blink(component.getTickTimeClose());
            }
        }
    }
}
