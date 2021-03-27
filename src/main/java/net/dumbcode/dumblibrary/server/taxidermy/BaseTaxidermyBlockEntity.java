package net.dumbcode.dumblibrary.server.taxidermy;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public abstract class BaseTaxidermyBlockEntity extends SimpleBlockEntity {

    @Getter private final TaxidermyHistory history = new TaxidermyHistory();

    public BaseTaxidermyBlockEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("History", this.history.writeToNBT(new CompoundNBT()));
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        this.history.readFromNBT(compound.getCompound("History"));
        super.load(state, compound);
    }

    public Map<String, TaxidermyHistory.CubeProps> getPoseData() {
        return this.history.getPoseData();
    }

    public abstract ResourceLocation getTexture();
    public abstract DCMModel getModel();

}
