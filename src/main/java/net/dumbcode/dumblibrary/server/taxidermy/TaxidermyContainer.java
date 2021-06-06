package net.dumbcode.dumblibrary.server.taxidermy;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

public class TaxidermyContainer extends Container {

    private final BaseTaxidermyBlockEntity blockEntity;

    public TaxidermyContainer(BaseTaxidermyBlockEntity blockEntity, int id) {
        super(DumbLibrary.TAXIDERMY_CONTAINER.get(), id);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return true;
    }

    public BaseTaxidermyBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
