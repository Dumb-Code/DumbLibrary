package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.XYZAxis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S5UpdateSkeletalBuilder implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private XYZAxis axis;
    private int type;
    private float value;

    public S5UpdateSkeletalBuilder() { }

    public S5UpdateSkeletalBuilder(BlockPos pos, String selectedPart, int type, XYZAxis axis, float value) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.part = selectedPart;
        this.type = type;
        this.axis = axis;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        type = buf.readByte();
        axis = XYZAxis.values()[buf.readInt()];
        value = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeByte(type);
        buf.writeInt(axis.ordinal());
        buf.writeFloat(value);
    }

    public static class Handler extends WorldModificationsMessageHandler<S5UpdateSkeletalBuilder, IMessage> {
        @Override
        protected void handleMessage(S5UpdateSkeletalBuilder message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BaseTaxidermyBlockEntity) {
                ((BaseTaxidermyBlockEntity)te).getHistory().liveEdit(message.part, message.type, message.axis, message.value);
            }
            pos.release();
        }
    }
}
