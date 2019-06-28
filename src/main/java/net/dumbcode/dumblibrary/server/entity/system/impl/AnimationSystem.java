package net.dumbcode.dumblibrary.server.entity.system.impl;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.entity.EntityFamily;
import net.dumbcode.dumblibrary.server.entity.EntityManager;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

//todo: remove
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = DumbLibrary.MODID)
public enum AnimationSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private AnimationComponent[] animations = new AnimationComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.ANIMATION);
        this.entities = family.getEntities();
        this.animations = family.populateBuffer(EntityComponentTypes.ANIMATION, this.animations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update() {
        for (int i = 0; i < this.entities.length; i++) {
            Entity entity = this.entities[i];
            AnimationComponent animation = this.animations[i];
            if(!entity.world.isRemote) { //Server side only. Client side is already handled
//                if(animation.getAnimationLayer() == null) {
//                    animation.createServersideLayer(entity);
//                }
            }
        }
    }
}
