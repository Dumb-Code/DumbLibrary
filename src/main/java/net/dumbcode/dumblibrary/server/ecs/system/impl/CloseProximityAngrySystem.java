package net.dumbcode.dumblibrary.server.ecs.system.impl;

import com.google.common.base.Predicate;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.CloseProximityAngryComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

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
            Predicate<Entity> withinRange = EntitySelectors.withinRange(entity.posX, entity.posY, entity.posZ, range);

            List<Entity> entities = world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(entity.getPosition()).grow(range, range, range),
                e -> withinRange.test(e) && component.getPredicate().test(e) && e instanceof EntityCreature
            );
            if(entities.isEmpty()) {
                component.setAngryAtEntity(null);
            } else {
                component.setAngryAtEntity(entities.get(0).getUniqueID());
            }
        }
    }
}
