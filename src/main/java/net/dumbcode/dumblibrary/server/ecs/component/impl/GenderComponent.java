package net.dumbcode.dumblibrary.server.ecs.component.impl;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.minecraft.nbt.NBTTagCompound;

public class GenderComponent extends EntityComponent implements RenderLocationComponent {
    public boolean male = false;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setBoolean("male", this.male);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.male = compound.getBoolean("male");
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.male);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.male = buf.readBoolean();
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        texture.addName(this.male ? "male" : "female", 30);
    }
}
