package net.dumbcode.dumblibrary.server.ecs.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.Random;

public class GenderComponent extends EntityComponent implements RenderLocationComponent, CanBreedComponent {
    public boolean male = new Random().nextBoolean();

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putBoolean("male", this.male);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.male = compound.getBoolean("male");
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeBoolean(this.male);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.male = buf.readBoolean();
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        texture.addName(() -> this.male ? "male" : "female", 30);
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return otherEntity.get(EntityComponentTypes.GENDER).map(g -> g.male != this.male).orElse(true);
    }
}
