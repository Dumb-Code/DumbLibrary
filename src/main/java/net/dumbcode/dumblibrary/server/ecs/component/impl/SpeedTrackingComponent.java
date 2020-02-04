package net.dumbcode.dumblibrary.server.ecs.component.impl;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

@Getter
@Setter
//Tracks horizontal movement
public class SpeedTrackingComponent extends EntityComponent {
    private double speed;
    private double previousSpeed;
}
