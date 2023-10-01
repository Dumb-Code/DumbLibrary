package net.dumbcode.dumblibrary.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SimpleBlockEntity extends BlockEntity {

    public SimpleBlockEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);
        return new SUpdateTileEntityPacket(this.worldPosition, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.level.getBlockState(this.worldPosition), pkt.getTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = new CompoundNBT();
        this.save(nbt);
        return nbt;
    }

    public void syncToClient() {
        if(this.level != null && !this.level.isClientSide) {
            for (PlayerEntity player : this.level.players()) {
                SUpdateTileEntityPacket packet = this.getUpdatePacket();
                if(packet != null) {
                    ((ServerPlayerEntity)player).connection.send(packet);
                }
            }
        }
    }
}
