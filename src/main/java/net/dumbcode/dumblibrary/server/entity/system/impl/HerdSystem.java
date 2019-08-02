package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.HerdSavedData;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.HerdComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.dumbcode.dumblibrary.server.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

public enum HerdSystem implements EntitySystem {

    INSTANCE;

    private Entity[] matchedEntities = new Entity[0];
    private HerdComponent[] herds = new HerdComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.HERD);
        this.herds = family.populateBuffer(EntityComponentTypes.HERD, this.herds);
        this.matchedEntities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.herds.length; i++) {
            Entity entity = this.matchedEntities[i];
            HerdComponent herd = this.herds[i];

            if(herd.herdUUID == null) {
                this.tryJoinNewHeard(entity, herd);

                if(herd.herdUUID == null) {
                    this.createNewHerd(entity, herd);
                }
            } else {
                this.moveAsHeard(entity, herd);
            }
        }
    }


    private void tryJoinNewHeard(Entity entity, HerdComponent herd) {
        for (Entity foundEntity : entity.world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(entity.getPosition()).grow(50, 50, 50), e -> e instanceof ComponentAccess
                && ((ComponentAccess) e).get(EntityComponentTypes.HERD).map(c -> c.herdTypeID.equals(herd.herdTypeID)).orElse(false))) {

            ComponentAccess ca = (ComponentAccess) foundEntity;
            HerdComponent foundHerd = ca.getOrNull(EntityComponentTypes.HERD);
            if(foundHerd != null && foundHerd.herdData != null) {
                foundHerd.addMember(entity.getUniqueID(), herd);
                break;
            }
        }
    }

    private void createNewHerd(Entity entity, HerdComponent herd) {
        System.out.println("Created Herd!");
        herd.herdUUID = UUID.randomUUID();
        this.ensureHerdData(entity, herd);

        herd.addMember(entity.getUniqueID(), herd);
        herd.herdData.leader = entity.getUniqueID();
    }

    //Currently, this moves all the entities 30 blocks away to where the leader is. This could maybe be changed.
    private void moveAsHeard(Entity entity, HerdComponent herd) {
        if(herd.herdData != null && entity.getUniqueID().equals(herd.herdData.leader)) {
            if(herd.herdData.tryMoveCooldown <= 0) {
                for (UUID uuid : herd.herdData.getMembers()) {
                    WorldUtils.getEntityFromUUID(entity.world, uuid)
                            .filter(e -> e.getDistance(entity) > 30 && e instanceof ComponentAccess)
                            .map(e -> Pair.of(e, ((ComponentAccess)e).get(EntityComponentTypes.HERD)))
                            .ifPresent(pair -> {
                                if(pair.getRight().isPresent()) {
                                    ((EntityLiving) entity).getNavigator().tryMoveToEntityLiving(pair.getLeft(), 0.1F);
                                    pair.getRight().get().herdData.tryMoveCooldown = 120;
                                }
                            });
                }
            }
            herd.herdData.tryMoveCooldown--;
        }
    }

    private void ensureHerdData(Entity entity, HerdComponent herd) {
        if(herd.herdUUID != null && herd.herdData == null) {
            herd.herdData = HerdSavedData.getData(entity.world, herd.herdUUID);
        }
    }

    @SubscribeEvent
    public void onEntityDie(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess) {
            HerdComponent component = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.HERD);
            if(component != null && component.herdData != null) {
                component.herdData.removeMember(entity.getUniqueID());
                if(component.herdData.leader.equals(entity.getUniqueID())) {
                    List<UUID> herdMembers = component.herdData.getMembers();
                    if(!herdMembers.isEmpty()) {
                        WorldUtils.getEntityFromUUID(entity.world, herdMembers.get(0)).ifPresent(e -> component.herdData.leader = e.getUniqueID());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDamaged(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        Entity source = event.getSource().getTrueSource();
        if(source !=  null && entity instanceof ComponentAccess) {
            HerdComponent component = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.HERD);
            if(component != null && component.herdData != null) {
                component.herdData.addEnemy(source.getUniqueID());
            }
        }
    }


}
