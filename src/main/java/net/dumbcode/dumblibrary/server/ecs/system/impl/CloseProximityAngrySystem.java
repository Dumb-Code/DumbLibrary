package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.CloseProximityAngryComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class CloseProximityAngrySystem implements EntitySystem {

    private Entity[] entities = new Entity[0];
    private CloseProximityAngryComponent[] components = new CloseProximityAngryComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.CLOSE_PROXIMITY_ANGRY);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(EntityComponentTypes.CLOSE_PROXIMITY_ANGRY, this.components);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            CloseProximityAngryComponent component = this.components[i];

            double range = component.getRange().getValue();
            Vector3d position = entity.position();
            Predicate<Entity> withinRange = EntityPredicates.withinDistance(position.x, position.y, position.z, range);

            List<Entity> entities = world.getEntities(entity, new AxisAlignedBB(entity.blockPosition()).inflate(range, range, range),
                e -> withinRange.test(e) && !(e instanceof ComposableCreatureEntity) && e instanceof CreatureEntity
            );
            if(entities.isEmpty()) {
                component.setAngryAtEntity(null);
            } else {
                component.setAngryAtEntity(entities.get(0).getUUID());
            }
        }
    }
}
