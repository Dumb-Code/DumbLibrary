package net.dumbcode.dumblibrary.server.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDispatcher;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class NetworkUtils {
    public static PlayerEntity getPlayer(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        return context.getDirection().getReceptionSide().isClient() ? getClientPlayer() : context.getSender();
    }

    @OnlyIn(Dist.CLIENT)
    private static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static PacketDistributor.PacketTarget forPos(IWorldReader world, BlockPos pos) {
        Chunk chunk = (Chunk) world.getChunk(pos);
        return PacketDistributor.TRACKING_CHUNK.with(() -> chunk);
    }
}
