package net.dumbcode.dumblibrary.server.taxidermy;

import net.dumbcode.dumblibrary.DumbLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

public class TaxidermyContainer extends AbstractContainerMenu {

    private final BaseTaxidermyBlockEntity blockEntity;

    public TaxidermyContainer(BaseTaxidermyBlockEntity blockEntity, int id) {
        super(DumbLibrary.TAXIDERMY_CONTAINER.get(), id);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean stillValid(Player p_75145_1_) {
        return true;
    }

    public BaseTaxidermyBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public static void open(ServerPlayer player, BaseTaxidermyBlockEntity blockEntity) {
        NetworkHooks.openScreen(player, create(blockEntity), blockEntity.getBlockPos());
    }

    public static MenuProvider create(BaseTaxidermyBlockEntity blockEntity) {
        return new SimpleMenuProvider((id, inv, p) -> new TaxidermyContainer(blockEntity, id), Component.literal(""));
    }
}
