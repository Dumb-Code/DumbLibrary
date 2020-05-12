package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class C8MoveInHistory implements IMessage {

    private int x;
    private int y;
    private int z;
    private int direction;

    public C8MoveInHistory() { }

    public C8MoveInHistory(BlockPos pos, int direction) {
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

    public static class Handler extends WorldModificationsMessageHandler<C8MoveInHistory, IMessage> {

        @Override
        protected void handleMessage(C8MoveInHistory message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?O
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)te;
                if(message.direction > 0) {
                    builder.getHistory().redo();
                } else if(message.direction < 0) {
                    builder.getHistory().undo();
                }
                DumbLibrary.NETWORK.sendToDimension(new S9UpdateHistoryIndex(pos, message.direction), world.provider.getDimension());
            }

            pos.release();
        }
    }
}
