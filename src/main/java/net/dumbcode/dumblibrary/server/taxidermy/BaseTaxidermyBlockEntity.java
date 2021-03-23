package net.dumbcode.dumblibrary.server.taxidermy;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.tabula.DCMModel;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;

public abstract class BaseTaxidermyBlockEntity extends SimpleBlockEntity {

    @Getter private final TaxidermyHistory history = new TaxidermyHistory();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("History", this.history.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.history.readFromNBT(compound.getCompoundTag("History"));
        super.readFromNBT(compound);
    }

    public Map<String, TaxidermyHistory.CubeProps> getPoseData() {
        return this.history.getPoseData();
    }

    public abstract ResourceLocation getTexture();
    public abstract DCMModel getModel();

}
