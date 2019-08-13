package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

@Getter
@Setter
//Tracks horizontal movement
public class SpeedTrackingComponent implements EntityComponent {
    private double speed;
    private double previousSpeed;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {

    }
}
