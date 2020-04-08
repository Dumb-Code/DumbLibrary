package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S9UpdateHistoryIndex implements IMessage {

    private int x;
    private int y;
    private int z;
    private int direction;

    public S9UpdateHistoryIndex() { }

    public S9UpdateHistoryIndex(BlockPos pos, int direction) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.direction = direction > 0 ? +1 : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        direction = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(direction);
    }

    public static class Handler extends WorldModificationsMessageHandler<S9UpdateHistoryIndex, IMessage> {
        @Override
        protected void handleMessage(S9UpdateHistoryIndex message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TaxidermyBlockEntity) {
                TaxidermyBlockEntity builder = (TaxidermyBlockEntity)te;
                if(message.direction > 0) {
                    builder.getHistory().redo();
                } else if(message.direction < 0) {
                    builder.getHistory().undo();
                }
            }

            pos.release();
        }
    }
}
