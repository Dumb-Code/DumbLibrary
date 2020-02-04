package net.dumbcode.dumblibrary.server.ecs.system.impl;

import net.dumbcode.dumblibrary.server.attributes.ModOp;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.EyesClosedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.LightAffectSleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.SleepingComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.UUID;

public enum LightSleepSystem implements EntitySystem {
    INSTANCE;

    private static final UUID SKYLIGHT_UUID = UUID.fromString("8f558d6c-d809-4382-9fc6-2c8895a1a0e0");
    private static final UUID BLOCKLIGHT_UUID = UUID.fromString("a66cc461-2472-47c6-9bea-63307d23588c");

    private Entity[] entities = new Entity[0];
    private LightAffectSleepingComponent[] components = new LightAffectSleepingComponent[0];
    private SleepingComponent[] sleepingComponents = new SleepingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.SLEEPING, EntityComponentTypes.LIGHT_AFFECT_SLEEPING);
        this.entities = family.getEntities();
        this.components = family.populateBuffer(EntityComponentTypes.LIGHT_AFFECT_SLEEPING, this.components);
        this.sleepingComponents = family.populateBuffer(EntityComponentTypes.SLEEPING, this.sleepingComponents);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.components.length; i++) {
            LightAffectSleepingComponent component = this.components[i];

            BlockPos pos = this.entities[i].getPosition();

            double skylight = world.getLightFor(EnumSkyBlock.SKY, pos) * this.calculateSkylightModifier(world.getWorldTime());
            double skylightValue = Math.max(component.getSkylightLevelStart().getIntValue() - skylight, 0) / component.getSkylightLevelStart().getValue();
            double blocklightValue = Math.max(component.getBlocklightLevelStart().getIntValue() - world.getLightFor(EnumSkyBlock.BLOCK, pos), 0) / component.getBlocklightLevelStart().getValue();

            ModifiableField constant = this.sleepingComponents[i].getTirednessChanceConstant();

            constant.addModifer(SKYLIGHT_UUID, ModOp.MULTIPLY_BASE_THEN_ADD, -(skylightValue - 0.5F) * 0.5F);
            constant.addModifer(BLOCKLIGHT_UUID, ModOp.MULTIPLY_BASE_THEN_ADD, -(blocklightValue - 0.5F) * 0.125F);

        }
    }

    //https://www.desmos.com/calculator/0v0r76smqz
    private double calculateSkylightModifier(long time) {
        return 1D - Math.max(exponentialCurve(time), exponentialCurve(time + 24000));
    }

    private double exponentialCurve(long time) {
        double expValue = (time - 18000D)/3000D;
        return Math.exp(-expValue*expValue);
    }
}
