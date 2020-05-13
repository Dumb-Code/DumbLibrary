package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.server.taxidermy.BaseTaxidermyBlockEntity;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.vecmath.Vector3f;

public class C6SkeletalMovement implements IMessage {

    private int x;
    private int y;
    private int z;
    private String part;
    private Vector3f rotations;
    private Vector3f position;

    public C6SkeletalMovement() { }

    public C6SkeletalMovement(BlockPos pos, String selectedPart, Vector3f rotations, Vector3f position) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.part = selectedPart;
        this.rotations = rotations;
        this.position = position;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        part = ByteBufUtils.readUTF8String(buf);
        rotations = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        position = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, part);
        buf.writeFloat(this.rotations.x);
        buf.writeFloat(this.rotations.y);
        buf.writeFloat(this.rotations.z);
        buf.writeFloat(this.position.x);
        buf.writeFloat(this.position.y);
        buf.writeFloat(this.position.z);
    }

    public static class Handler extends WorldModificationsMessageHandler<C6SkeletalMovement, IMessage> {
        @Override
        public void handleMessage(C6SkeletalMovement message, MessageContext ctx, World world, EntityPlayer player) {
            // FIXME: security checks?
            BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(message.x, message.y, message.z);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BaseTaxidermyBlockEntity) {
                BaseTaxidermyBlockEntity builder = (BaseTaxidermyBlockEntity)te;
                builder.getHistory().add(new TaxidermyHistory.Record(message.part, new TaxidermyHistory.CubeProps(message.rotations, message.position)));
            }
            DumbLibrary.NETWORK.sendToDimension(new S7HistoryRecord(pos, message.part, message.rotations, message.position), world.provider.getDimension());

            pos.release();
        }
    }
}
