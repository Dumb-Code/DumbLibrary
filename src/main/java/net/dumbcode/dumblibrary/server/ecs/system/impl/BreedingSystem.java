package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.BreedingResultComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.BreedingComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class BreedingSystem implements EntitySystem {

    private Entity[] entities = new Entity[0];
    private BreedingComponent[] breeding = new BreedingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.BREEDING);
        this.entities = family.getEntities();
        this.breeding = family.populateBuffer(EntityComponentTypes.BREEDING, this.breeding);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            if(entity instanceof ComponentAccess) {
                List<CanBreedComponent> canBreedComponents = ((ComponentAccess) entity).getAllComponents()
                    .stream()
                    .filter(e -> e instanceof CanBreedComponent)
                    .map(e -> (CanBreedComponent) e)
                    .collect(Collectors.toList());

                this.breeding[i].setTicksSinceLastBreed(this.breeding[i].getTicksSinceLastBreed() + 1);

                int searchRadXZ = 20;
                int searchRadY = 5;
                ComponentAccess mate = null;
                List<Entity> entities = world.getEntities(entity, new AxisAlignedBB(-searchRadXZ, -searchRadY, -searchRadXZ, searchRadXZ, searchRadY, searchRadXZ).move(entity.position()), e -> e instanceof ComponentAccess);
                for (Entity otherEntity : entities) {
                    ComponentAccess oca = (ComponentAccess) otherEntity;
                    if(oca.contains(EntityComponentTypes.BREEDING) && canBreedComponents.stream().allMatch(c -> c.canBreedWith(oca))) {
                         mate = oca;
                         break;
                    }
                }
                if(mate != null) {
                    mate.getOrExcept(EntityComponentTypes.BREEDING).setTicksSinceLastBreed(0);
                    this.breeding[i].setTicksSinceLastBreed(0);

                    for (EntityComponent component : mate.getAllComponents()) {
                        if(component instanceof BreedingResultComponent) {
                            ((BreedingResultComponent) component).onBreed(mate, (ComponentAccess) entity);
                        }
                    }
                    for (EntityComponent component : ((ComponentAccess) entity).getAllComponents()) {
                        if(component instanceof BreedingResultComponent) {
                            ((BreedingResultComponent) component).onBreed((ComponentAccess) entity, mate);
                        }
                    }
                }
            }
        }
    }
}
