package net.dumbcode.dumblibrary.server.ecs.component.impl;

import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Random;

public class GenderComponent extends EntityComponent implements RenderLocationComponent, CanBreedComponent {
    public boolean male = new Random().nextBoolean();

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.putBoolean("male", this.male);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        super.deserialize(compound);
        this.male = compound.getBoolean("male");
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeBoolean(this.male);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf) {
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
