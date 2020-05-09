package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.utils.RotationAxis;
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
    private RotationAxis axis;
    private float newAngle;

    public S5UpdateSkeletalBuilder() { }

    public S5UpdateSkeletalBuilder(BlockPos pos, String selectedPart, RotationAxis axis, float newAngle) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.part = selectedPart;
        this.axis = axis;
        this.newAngle = newAngle;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        axis = RotationAxis.values()[buf.readInt()];
        newAngle = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeInt(axis.ordinal());
        buf.writeFloat(newAngle);
    }

    public static class Handler extends WorldModificationsMessageHandler<S5UpdateSkeletalBuilder, IMessage> {
        @Override
        protected void handleMessage(S5UpdateSkeletalBuilder message, MessageContext ctx, World world, EntityPlayer player) {
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TaxidermyBlockEntity) {
                ((TaxidermyBlockEntity)te).getHistory().liveEdit(message.part, message.axis, message.newAngle);
            }
            pos.release();
        }
    }
}
