package net.dumbcode.dumblibrary.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class S1PlayItemCrackParticle implements IMessage {

    private Vec3d pos;
    private Vec3d speed;
    private ItemStack stack;

    public S1PlayItemCrackParticle() {
    }

    public S1PlayItemCrackParticle(Vec3d pos, Vec3d speed, ItemStack stack) {
        this.pos = pos;
        this.speed = speed;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.speed = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.pos.x);
        buf.writeDouble(this.pos.y);
        buf.writeDouble(this.pos.z);

        buf.writeDouble(this.speed.x);
        buf.writeDouble(this.speed.y);
        buf.writeDouble(this.speed.z);

        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    public static class Handler extends WorldModificationsMessageHandler<S1PlayItemCrackParticle,S1PlayItemCrackParticle> {

        @Override
        protected void handleMessage(S1PlayItemCrackParticle message, MessageContext ctx, World world, EntityPlayer player) {
            Particle particle = new ParticleBreaking.Factory().createParticle(-1, world, message.pos.x, message.pos.y, message.pos.z, message.speed.x, message.speed.y, message.speed.z, 1);
            particle.setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(message.stack).getParticleTexture());
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }
}
