package net.dumbcode.dumblibrary.server.taxidermy;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

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

    public static void open(ServerPlayerEntity player, BaseTaxidermyBlockEntity blockEntity) {
        NetworkHooks.openGui(player, create(blockEntity), blockEntity.getBlockPos());
    }

    public static INamedContainerProvider create(BaseTaxidermyBlockEntity blockEntity) {
        return new SimpleNamedContainerProvider((id, inv, p) -> new TaxidermyContainer(blockEntity, id), new StringTextComponent(""));
    }
}
